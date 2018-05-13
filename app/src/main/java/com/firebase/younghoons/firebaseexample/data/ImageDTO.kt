package com.firebase.younghoons.firebaseexample.data

data class ImageDTO(var imageUrl: String,var imageName: String, var title : String, var description : String, var uId : String, var userId : String, var favoriteCount: Int = 0, var favorites: HashMap<String, Boolean> = HashMap()){
    constructor(): this("","","","","","",0,HashMap())
}