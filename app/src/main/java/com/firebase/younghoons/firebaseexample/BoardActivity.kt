package com.firebase.younghoons.firebaseexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebase.younghoons.firebaseexample.data.ImageDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


class BoardActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    var imageDTOs: ArrayList<ImageDTO> = ArrayList()
    var uidLists: ArrayList<String> = ArrayList()
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        recyclerView = findViewById(R.id.recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        var boardRecyclerViewAdapter = BoardRecyclerViewAdapter()
        recyclerView.adapter = boardRecyclerViewAdapter

        database.reference.child("images").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                imageDTOs.clear()
                uidLists.clear()
                for (snapshot: DataSnapshot in dataSnapshot!!.children.iterator()) {
                    val imageDTO: ImageDTO? = snapshot.getValue(ImageDTO::class.java)
                    val uidKey = snapshot.key
                    if (imageDTO != null) {
                        imageDTOs.add(imageDTO)
                        uidLists.add(uidKey)
                    }
                }
                boardRecyclerViewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError?) {

            }


        })
    }

    inner class BoardRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view: View = LayoutInflater.from(parent.context!!).inflate(R.layout.item_board, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is CustomViewHolder) {
                holder.titleView.text = imageDTOs[position].title
                holder.description.text = imageDTOs.get(position).description
                Glide.with(holder.imageView).load(imageDTOs[position].imageUrl).into(holder.imageView)
                holder.favoriteButton.setOnClickListener {
                    onStarClicked(database.reference.child("images").child(uidLists[position]))
                }

                if (imageDTOs[position].favorites.containsKey(auth.currentUser?.uid)) {
                    holder.favoriteButton.setImageResource(R.drawable.favorite_black)
                } else {
                    holder.favoriteButton.setImageResource(R.drawable.favorite_border_black)
                }

                holder.deleteButton.setOnClickListener{
                    deleteContent(position)
                }
            }

        }

        override fun getItemCount(): Int {
            return imageDTOs.size
        }

        private fun onStarClicked(postRef: DatabaseReference) {
            postRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    var imageDTO = mutableData.getValue(ImageDTO::class.java)
                            ?: return Transaction.success(mutableData)

                    if (imageDTO.favorites.containsKey(auth.currentUser?.uid)) {
                        imageDTO.favorites
                        imageDTO.favoriteCount = imageDTO.favoriteCount - 1
                        imageDTO.favorites.remove(auth.currentUser?.uid)
                    } else {
                        imageDTO.favoriteCount = imageDTO.favoriteCount + 1
                        imageDTO.favorites[auth.currentUser?.uid!!] = true
                    }

                    // Set value and report transaction success
                    mutableData.value = imageDTO
                    return Transaction.success(mutableData)
                }

                override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                        dataSnapshot: DataSnapshot?) {
                }
            })
        }


        private fun deleteContent(position: Int) {
            storage.reference.child("images").child(imageDTOs[position].imageName).delete().addOnSuccessListener {
                database.reference.child("images").child(uidLists[position]).removeValue().addOnSuccessListener {
                    Toast.makeText(this@BoardActivity, "삭제 완료", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener({ Toast.makeText(this@BoardActivity, "삭제 실패", Toast.LENGTH_LONG).show() })
//            database.reference.child("images").child("디비 키값").setValue(null) 삭제 두가지 방법 setValue null 또는 remove
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var imageView: ImageView = view.findViewById(R.id.item_imageView)
            var titleView: TextView = view.findViewById(R.id.item_title)
            var description: TextView = view.findViewById(R.id.item_description)
            var favoriteButton: ImageView = view.findViewById(R.id.item_favorite)
            var deleteButton : ImageView = view.findViewById(R.id.item_delete)
        }

    }
}
