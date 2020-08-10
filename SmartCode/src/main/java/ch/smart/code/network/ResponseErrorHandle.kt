package ch.smart.code.network

interface ResponseErrorHandle {
    
    /**
     * 处理异常情况
     *
     * @param showMsg 是否弹出提示信息
     * @return True：事件已处理，不进行分发 False：未处理，继续分发
     */
    fun errorHandle(throwable: Throwable, showMsg: Boolean): Boolean
    
}
