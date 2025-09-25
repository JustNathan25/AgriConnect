package com.example.agriconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.agriconnect.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CreateListingActivity : AppCompatActivity() {

    private lateinit var editProductName: EditText
    private lateinit var editProductPrice: EditText
    private lateinit var spinnerCondition: Spinner
    private lateinit var editProductDescription: EditText
    private lateinit var btnSelectImages: Button
    private lateinit var layoutImagePreview: LinearLayout
    private lateinit var btnPickLocation: Button
    private lateinit var textSelectedLocation: TextView
    private lateinit var btnSubmitListing: Button
    private lateinit var progressBarUpload: ProgressBar

    private val imageUris = mutableListOf<Uri>()
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    private var selectedAddress: String? = null

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickLocationLauncher: ActivityResultLauncher<Intent>

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance() // Use default bucket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        editProductName = findViewById(R.id.editProductName)
        editProductPrice = findViewById(R.id.editProductPrice)
        spinnerCondition = findViewById(R.id.spinnerCondition)
        editProductDescription = findViewById(R.id.editProductDescription)
        btnSelectImages = findViewById(R.id.btnSelectImages)
        layoutImagePreview = findViewById(R.id.layoutImagePreview)
        btnPickLocation = findViewById(R.id.btnPickLocation)
        textSelectedLocation = findViewById(R.id.textSelectedLocation)
        btnSubmitListing = findViewById(R.id.btnSubmitListing)
        progressBarUpload = findViewById(R.id.progressBarUpload)

        setupConditionSpinner()
        setupLaunchers()

        btnSelectImages.setOnClickListener { selectImages() }
        btnPickLocation.setOnClickListener { pickLocation() }
        btnSubmitListing.setOnClickListener { submitListing() }
    }

    private fun setupConditionSpinner() {
        val conditions = listOf("New", "Used - Like New", "Used - Good", "Used - Fair")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, conditions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCondition.adapter = adapter
    }

    private fun setupLaunchers() {
        pickImagesLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUris.clear()
                layoutImagePreview.removeAllViews()
                val data = result.data
                data?.clipData?.let {
                    val count = minOf(it.itemCount, 5)
                    for (i in 0 until count) {
                        val uri = it.getItemAt(i).uri
                        imageUris.add(uri)
                        addImagePreview(uri)
                    }
                } ?: data?.data?.let { uri ->
                    imageUris.add(uri)
                    addImagePreview(uri)
                }
            }
        }

        pickLocationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                selectedLat = data?.getDoubleExtra("latitude", 0.0)
                selectedLng = data?.getDoubleExtra("longitude", 0.0)
                selectedAddress = data?.getStringExtra("address")
                textSelectedLocation.text = selectedAddress ?: "Location selected"
            }
        }
    }

    private fun selectImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select up to 5 images"))
    }

    private fun pickLocation() {
        val intent = Intent(this, MapPickerActivity::class.java)
        pickLocationLauncher.launch(intent)
    }

    private fun submitListing() {
        val name = editProductName.text.toString().trim()
        val price = editProductPrice.text.toString().toDoubleOrNull()
        val condition = spinnerCondition.selectedItem?.toString() ?: "New"
        val description = editProductDescription.text.toString().trim()

        if (name.isEmpty() || price == null || imageUris.isEmpty() || selectedLat == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmitListing.isEnabled = false
        val listingId = firestore.collection("products").document().id

        uploadImages(listingId) { imageUrls ->
            val product = Product(
                id = listingId,
                name = name,
                price = price,
                condition = condition,
                description = description,
                images = imageUrls,
                locationName = selectedAddress,
                latitude = selectedLat,
                longitude = selectedLng,
                sellerId = FirebaseAuth.getInstance().currentUser?.uid
            )

            firestore.collection("products").document(listingId)
                .set(product)
                .addOnSuccessListener {
                    Toast.makeText(this, "Listing added!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save listing", Toast.LENGTH_SHORT).show()
                    btnSubmitListing.isEnabled = true
                }
        }
    }

    private fun uploadImages(listingId: String, callback: (List<String>) -> Unit) {
        val storageRef = storage.reference.child("product_images/$listingId")
        val imageUrls = mutableListOf<String>()

        var completedUploads = 0
        progressBarUpload.progress = 0
        progressBarUpload.visibility = ProgressBar.VISIBLE

        for (uri in imageUris) {
            if (uri == null) continue
            val fileRef = storageRef.child("${UUID.randomUUID()}.jpg")
            val uploadTask = fileRef.putFile(uri)

            uploadTask.addOnProgressListener { snapshot ->
                val percent = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                val overallPercent = ((completedUploads * 100 + percent) / imageUris.size)
                progressBarUpload.progress = overallPercent
            }.continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                fileRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                imageUrls.add(downloadUri.toString())
                completedUploads++
                if (completedUploads == imageUris.size) {
                    progressBarUpload.visibility = ProgressBar.GONE
                    btnSubmitListing.isEnabled = true
                    callback(imageUrls)
                }
            }.addOnFailureListener { e ->
                progressBarUpload.visibility = ProgressBar.GONE
                btnSubmitListing.isEnabled = true
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addImagePreview(uri: Uri) {
        val imageView = ImageView(this).apply {
            setImageURI(uri)
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { setMargins(8,0,8,0) }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        layoutImagePreview.addView(imageView)
    }
}
