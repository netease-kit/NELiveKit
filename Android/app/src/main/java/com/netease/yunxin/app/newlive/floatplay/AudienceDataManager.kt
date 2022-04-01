package com.netease.yunxin.app.newlive.floatplay

import com.netease.yunxin.kit.alog.ALog

object AudienceDataManager {
    private var roomId=""
    private var data: AudienceData?=null
    private val TAG="AudienceDataManager"
    fun setRoomId(roomId:String){
        AudienceDataManager.roomId =roomId
    }

    fun getRoomId():String{
        return roomId
    }

    fun setDataToCache(data: AudienceData){
        AudienceDataManager.data =data
    }

    fun getDataFromCache(): AudienceData?{
        return data
    }

    fun hasCache(roomId: String):Boolean{
        return roomId == getRoomId() && getDataFromCache() != null
                &&roomId== getDataFromCache()?.liveInfo?.live?.roomUuid
    }

    fun clear(){
        roomId =""
        data =null
        ALog.d(TAG,"clear()")
    }
}