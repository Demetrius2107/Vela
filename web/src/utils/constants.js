// Vela IM 前端常量定义
export const VELA = {
  APP_NAME: 'Vela IM',
  API_PREFIX: '/v1',
  WS_URL: import.meta.env.VITE_WS_URL || 'ws://localhost:19000'
}

export const COMMAND = {
  // 消息命令
  MSG: {
    P2P: 0x444,
    GROUP: 0x445,
    ACK: 0x446,
    READED: 0x447,
    RECALL: 0x448
  },
  // 好友命令
  FRIEND: {
    ADD: 0x333,
    DELETE: 0x334,
    APPROVE: 0x335
  },
  // 群组命令
  GROUP: {
    CREATE: 0x555,
    JOIN: 0x556,
    LEAVE: 0x557
  }
}

export const STORAGE_KEYS = {
  TOKEN: 'vela_token',
  USER_ID: 'vela_user_id',
  USER_INFO: 'vela_user_info'
}
