package com.ksballetba.ibus.data.entity

class CustomOtherStep(val mStepType:Int,val mInstruction:String?){
    companion object {
        const val DRIVING_TYPE = 1
        const val WALKING_TYPE = 2
        const val BIKING_TYPE = 3
    }
}