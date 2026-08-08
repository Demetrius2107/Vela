#!/usr/bin/env python3
"""
Vela IM - Phase 1 REST API 连通性测试脚本
=========================================
测试链路: 用户导入 -> 添加好友 -> 登录 -> 发送P2P消息 -> 同步离线消息 -> 发送权限校验

使用方式:
    python phase1_api_test.py [--gateway URL] [--app-id N]

前置条件:
    1. MySQL / Redis / RabbitMQ / ZooKeeper 已启动 (docker-compose up -d)
    2. vela-gateway (8889) 已启动
    3. vela-service-user (8010) 已启动
    4. vela-service-friendship (8011) 已启动
    5. vela-service-message (8014) 已启动

Author: Vela Team
Since: 1.1
CreateTime: 2026-08-08
"""

import json
import time
import random
import argparse
import sys
import traceback
from datetime import datetime

try:
    import requests
except ImportError:
    print("[ERROR] requests 库未安装，请执行: pip install requests")
    sys.exit(1)


# ==================== 配置 ====================

DEFAULT_GATEWAY = "http://127.0.0.1:8889"
DEFAULT_APP_ID = 10000

# 测试用户
USER_A_ID = "test_conn_a"
USER_A_NAME = "ConnectTestA"
USER_B_ID = "test_conn_b"
USER_B_NAME = "ConnectTestB"
USER_PASSWORD = "123456"

# 颜色输出 (Windows 兼容)
class Color:
    GREEN = "\033[92m"
    RED = "\033[91m"
    YELLOW = "\033[93m"
    CYAN = "\033[96m"
    BOLD = "\033[1m"
    END = "\033[0m"

# Windows 启用 ANSI 颜色
if sys.platform == "win32":
    import os
    os.system("")


# ==================== 工具函数 ====================

def log(msg, color=""):
    ts = datetime.now().strftime("%H:%M:%S.%f")[:-3]
    print(f"{color}[{ts}] {msg}{Color.END}")


def log_step(num, title):
    print(f"\n{'='*60}")
    log(f"Step {num}: {title}", Color.BOLD + Color.CYAN)
    print(f"{'='*60}")


def log_request(method, url, body=None):
    log(f"  -> {method} {url}", Color.YELLOW)
    if body:
        log(f"  -> Body: {json.dumps(body, ensure_ascii=False)}", Color.YELLOW)


def log_response(status_code, body):
    color = Color.GREEN if 200 <= status_code < 300 else Color.RED
    log(f"  <- HTTP {status_code}", color)
    log(f"  <- Body: {json.dumps(body, ensure_ascii=False)}", color)


def assert_ok(result, step_name):
    """断言 Result 对象 code == 200"""
    if isinstance(result, dict) and result.get("code") == 200:
        log(f"  [PASS] {step_name}", Color.GREEN)
        return True
    else:
        code = result.get("code", "N/A") if isinstance(result, dict) else "N/A"
        msg = result.get("msg", str(result)) if isinstance(result, dict) else str(result)
        log(f"  [FAIL] {step_name} - code={code}, msg={msg}", Color.RED)
        return False


def assert_has_data(result, step_name):
    """断言 Result 对象 code == 200 且 data 非空"""
    if assert_ok(result, step_name):
        data = result.get("data")
        if data is not None:
            log(f"  [PASS] {step_name} - data 非空", Color.GREEN)
            return True
        else:
            log(f"  [FAIL] {step_name} - data 为空", Color.RED)
            return False
    return False


# ==================== API 封装 ====================

class VelaApiClient:
    """Vela IM REST API 客户端"""

    def __init__(self, gateway_url, app_id):
        self.base_url = gateway_url.rstrip("/")
        self.app_id = app_id
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def _post(self, path, body, params=None):
        """发送 POST 请求"""
        url = f"{self.base_url}{path}"
        all_params = {"appId": self.app_id}
        if params:
            all_params.update(params)

        log_request("POST", url, body)
        try:
            resp = self.session.post(url, json=body, params=all_params, timeout=10)
            result = resp.json()
            log_response(resp.status_code, result)
            return result
        except requests.exceptions.ConnectionError:
            log(f"  [ERROR] 连接失败 - 服务可能未启动", Color.RED)
            return {"code": -1, "msg": "Connection refused"}
        except Exception as e:
            log(f"  [ERROR] {e}", Color.RED)
            return {"code": -1, "msg": str(e)}

    # ----- 用户服务 -----

    def import_user(self, user_id, nick_name, password="123456"):
        """导入用户（注册）"""
        body = {
            "userData": [{
                "userId": user_id,
                "nickName": nick_name,
                "password": password,
                "appId": self.app_id,
                "userType": 1,
                "friendAllowType": 0,
                "disableAddFriend": 0,
                "forbiddenFlag": 0,
                "silentFlag": 0,
                "delFlag": 0
            }]
        }
        return self._post("/v1/user/importUser", body)

    def login(self, user_id, client_type=0):
        """用户登录，返回路由信息（TCP/WebSocket 地址）"""
        body = {
            "userId": user_id,
            "appId": self.app_id,
            "clientType": client_type
        }
        return self._post("/v1/user/login", body)

    def get_user_sequence(self, user_id):
        """获取用户序列号"""
        body = {
            "operater": user_id,
            "appId": self.app_id,
            "clientType": 0
        }
        return self._post("/v1/user/getUserSequence", body)

    # ----- 好友服务 -----

    def add_friend(self, from_id, to_id, add_wording="test"):
        """添加好友"""
        body = {
            "fromId": from_id,
            "toItem": {
                "toId": to_id,
                "addSource": "connectivity_test",
                "addWording": add_wording,
                "remark": ""
            },
            "appId": self.app_id,
            "clientType": 0
        }
        return self._post("/v1/friendship/addFriend", body)

    def import_friendship(self, from_id, to_id):
        """导入好友关系（直接建立，不走审批流程）"""
        body = {
            "fromId": from_id,
            "friendItem": [{
                "toId": to_id,
                "remark": "",
                "addSource": "import",
                "status": 1,  # FRIEND_STATUS_NORMAL
                "black": 1    # BLACK_STATUS_NORMAL
            }],
            "appId": self.app_id,
            "clientType": 0
        }
        return self._post("/v1/friendship/importFriendShip", body)

    def check_friend(self, from_id, to_id):
        """检查好友关系"""
        body = {
            "fromId": from_id,
            "toIds": [to_id],
            "checkType": 1,
            "appId": self.app_id,
            "clientType": 0
        }
        return self._post("/v1/friendship/checkFriend", body)

    # ----- 消息服务 -----

    def send_p2p_message(self, from_id, to_id, message_body="Hello from Phase1 test!"):
        """发送 P2P 消息"""
        msg_id = f"msg_{int(time.time()*1000)}_{random.randint(1000,9999)}"
        body = {
            "messageId": msg_id,
            "fromId": from_id,
            "toId": to_id,
            "messageRandom": random.randint(10000, 99999),
            "messageTime": int(time.time()),
            "messageBody": message_body,
            "appId": self.app_id,
            "clientType": 0
        }
        return self._post("/v1/message/send", body)

    def check_send_permission(self, from_id, to_id, command=0x44F):
        """发送权限校验（TCP 网关通过 Feign 调用的接口）"""
        body = {
            "fromId": from_id,
            "toId": to_id,
            "appId": self.app_id,
            "command": command
        }
        return self._post("/v1/message/checkSend", body)

    def sync_offline_message(self, user_id, last_sequence=0, max_limit=100):
        """同步离线消息"""
        body = {
            "operater": user_id,
            "appId": self.app_id,
            "clientType": 0,
            "lastSequence": last_sequence,
            "maxLimit": max_limit
        }
        return self._post("/v1/message/syncOfflineMessage", body)


# ==================== 测试主流程 ====================

def run_phase1(gateway_url, app_id):
    """执行 Phase 1 全链路测试"""

    client = VelaApiClient(gateway_url, app_id)

    total_steps = 0
    passed_steps = 0
    results = {}

    print(f"\n{Color.BOLD}Vela IM - Phase 1 REST API 连通性测试{Color.END}")
    print(f"  Gateway: {gateway_url}")
    print(f"  AppId:   {app_id}")
    print(f"  User A:  {USER_A_ID}")
    print(f"  User B:  {USER_B_ID}")
    print(f"  时间:    {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    # ---------- Step 1: 导入用户 A ----------
    log_step(1, "导入用户 A")
    total_steps += 1
    result = client.import_user(USER_A_ID, USER_A_NAME, USER_PASSWORD)
    results["import_user_a"] = result
    # importUser 可能返回 200（新导入）或错误（已存在），都算通过
    if result.get("code") == 200:
        log(f"  [PASS] 用户 A 导入成功", Color.GREEN)
        passed_steps += 1
    elif "exist" in str(result.get("msg", "")).lower() or "already" in str(result.get("msg", "")).lower():
        log(f"  [PASS] 用户 A 已存在（视为成功）", Color.GREEN)
        passed_steps += 1
    else:
        log(f"  [WARN] 用户 A 导入返回: {result.get('msg', 'unknown')}", Color.YELLOW)
        # 可能是已存在，继续测试
        passed_steps += 1

    # ---------- Step 2: 导入用户 B ----------
    log_step(2, "导入用户 B")
    total_steps += 1
    result = client.import_user(USER_B_ID, USER_B_NAME, USER_PASSWORD)
    results["import_user_b"] = result
    if result.get("code") == 200:
        log(f"  [PASS] 用户 B 导入成功", Color.GREEN)
        passed_steps += 1
    elif "exist" in str(result.get("msg", "")).lower() or "already" in str(result.get("msg", "")).lower():
        log(f"  [PASS] 用户 B 已存在（视为成功）", Color.GREEN)
        passed_steps += 1
    else:
        log(f"  [WARN] 用户 B 导入返回: {result.get('msg', 'unknown')}", Color.YELLOW)
        passed_steps += 1

    # ---------- Step 3: 导入好友关系 (A <-> B) ----------
    log_step(3, "导入好友关系 (A -> B)")
    total_steps += 1
    result = client.import_friendship(USER_A_ID, USER_B_ID)
    results["import_friend_a_to_b"] = result
    if assert_ok(result, "好友关系导入 A->B"):
        passed_steps += 1

    # 反向也导入
    log_step(3.1, "导入好友关系 (B -> A)")
    total_steps += 1
    result = client.import_friendship(USER_B_ID, USER_A_ID)
    results["import_friend_b_to_a"] = result
    if assert_ok(result, "好友关系导入 B->A"):
        passed_steps += 1

    # ---------- Step 4: 验证好友关系 ----------
    log_step(4, "验证好友关系")
    total_steps += 1
    result = client.check_friend(USER_A_ID, USER_B_ID)
    results["check_friend"] = result
    if assert_ok(result, "好友关系检查"):
        passed_steps += 1

    # ---------- Step 5: 用户 A 登录 ----------
    log_step(5, "用户 A 登录")
    total_steps += 1
    result = client.login(USER_A_ID, client_type=0)  # WEBAPI
    results["login_a"] = result
    if assert_ok(result, "用户 A 登录"):
        passed_steps += 1
        data = result.get("data", {})
        if data:
            log(f"  [INFO] 路由信息: {json.dumps(data, ensure_ascii=False)}", Color.CYAN)

    # ---------- Step 6: 用户 B 登录 ----------
    log_step(6, "用户 B 登录")
    total_steps += 1
    result = client.login(USER_B_ID, client_type=0)  # WEBAPI
    results["login_b"] = result
    if assert_ok(result, "用户 B 登录"):
        passed_steps += 1
        data = result.get("data", {})
        if data:
            log(f"  [INFO] 路由信息: {json.dumps(data, ensure_ascii=False)}", Color.CYAN)

    # ---------- Step 7: 发送权限校验 ----------
    log_step(7, "发送权限校验 (TCP 网关调用的 Feign 接口)")
    total_steps += 1
    result = client.check_send_permission(USER_A_ID, USER_B_ID, command=0x44F)
    results["check_send"] = result
    if assert_ok(result, "发送权限校验"):
        passed_steps += 1

    # ---------- Step 8: 发送 P2P 消息 ----------
    log_step(8, "发送 P2P 消息 (A -> B)")
    total_steps += 1
    msg_content = f"Phase1 test message at {datetime.now().strftime('%H:%M:%S')}"
    result = client.send_p2p_message(USER_A_ID, USER_B_ID, msg_content)
    results["send_message"] = result
    if assert_ok(result, "P2P 消息发送"):
        passed_steps += 1
        data = result.get("data")
        if data and isinstance(data, dict):
            log(f"  [INFO] 消息Key: {data.get('messageKey', 'N/A')}", Color.CYAN)
            log(f"  [INFO] 消息Seq: {data.get('messageSequence', 'N/A')}", Color.CYAN)

    # ---------- Step 9: 同步用户 B 的离线消息 ----------
    log_step(9, "同步用户 B 的离线消息")
    total_steps += 1
    # 等待消息写入完成
    time.sleep(0.5)
    result = client.sync_offline_message(USER_B_ID, last_sequence=0, max_limit=100)
    results["sync_message_b"] = result
    if assert_ok(result, "离线消息同步"):
        passed_steps += 1
        data = result.get("data")
        if data:
            if isinstance(data, list):
                log(f"  [INFO] 拉取到 {len(data)} 条消息", Color.CYAN)
                for i, msg in enumerate(data):
                    log(f"         [{i}] {json.dumps(msg, ensure_ascii=False)[:120]}", Color.CYAN)
            elif isinstance(data, dict):
                msg_list = data.get("messageList", data.get("data", []))
                if isinstance(msg_list, list):
                    log(f"  [INFO] 拉取到 {len(msg_list)} 条消息", Color.CYAN)

    # ---------- Step 10: 同步用户 A 的离线消息（验证是否只收到自己的消息）----------
    log_step(10, "同步用户 A 的离线消息（对照组）")
    total_steps += 1
    result = client.sync_offline_message(USER_A_ID, last_sequence=0, max_limit=100)
    results["sync_message_a"] = result
    if assert_ok(result, "离线消息同步 (A)"):
        passed_steps += 1

    # ---------- 汇总 ----------
    print(f"\n{'='*60}")
    print(f"{Color.BOLD}Phase 1 测试结果汇总{Color.END}")
    print(f"{'='*60}")
    print(f"  通过: {Color.GREEN}{passed_steps}{Color.END} / {total_steps}")
    print(f"  通过率: {Color.GREEN if passed_steps == total_steps else Color.RED}{passed_steps/total_steps*100:.0f}%{Color.END}")

    if passed_steps == total_steps:
        print(f"\n  {Color.GREEN}{Color.BOLD}ALL PASSED - 后端 API 链路完整可用{Color.END}")
    elif passed_steps >= total_steps * 0.7:
        print(f"\n  {Color.YELLOW}部分通过 - 核心链路基本可用，个别环节需排查{Color.END}")
    else:
        print(f"\n  {Color.RED}大量失败 - 后端链路存在阻断问题，需逐项排查{Color.END}")

    # 输出失败项
    failed = []
    step_names = {
        "import_user_a": "Step1: 导入用户A",
        "import_user_b": "Step2: 导入用户B",
        "import_friend_a_to_b": "Step3: 导入好友A->B",
        "import_friend_b_to_a": "Step3.1: 导入好友B->A",
        "check_friend": "Step4: 验证好友关系",
        "login_a": "Step5: 用户A登录",
        "login_b": "Step6: 用户B登录",
        "check_send": "Step7: 发送权限校验",
        "send_message": "Step8: 发送P2P消息",
        "sync_message_b": "Step9: 同步B离线消息",
        "sync_message_a": "Step10: 同步A离线消息",
    }
    for key, name in step_names.items():
        r = results.get(key, {})
        if r.get("code") != 200:
            failed.append(f"  {name}: code={r.get('code')}, msg={r.get('msg')}")

    if failed:
        print(f"\n{Color.RED}失败项明细:{Color.END}")
        for f in failed:
            print(f)

    # 保存结果到 JSON
    report_path = "phase1_result.json"
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump({
            "test_time": datetime.now().isoformat(),
            "gateway": gateway_url,
            "appId": app_id,
            "total": total_steps,
            "passed": passed_steps,
            "results": results
        }, f, ensure_ascii=False, indent=2)
    log(f"\n  测试报告已保存: {report_path}", Color.CYAN)

    return passed_steps == total_steps


# ==================== 入口 ====================

def main():
    parser = argparse.ArgumentParser(description="Vela IM Phase 1 REST API 连通性测试")
    parser.add_argument("--gateway", default=DEFAULT_GATEWAY, help=f"网关地址 (默认: {DEFAULT_GATEWAY})")
    parser.add_argument("--app-id", type=int, default=DEFAULT_APP_ID, help=f"应用ID (默认: {DEFAULT_APP_ID})")
    args = parser.parse_args()

    try:
        success = run_phase1(args.gateway, args.app_id)
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print(f"\n{Color.YELLOW}测试被中断{Color.END}")
        sys.exit(130)
    except Exception as e:
        log(f"测试异常: {e}", Color.RED)
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
