package edu.stanford.meganliu.simpleyelp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "uLos4-6vDxIzNCv4v5DcMx5JkPw0VOdq8nZAtreswOr2pOY-3I5tnLNDLXIklZ7q3hl_AiZWd7T2qpFYUHjYRwSUG3IDk43ARuW9ngNIGNMugxWJf92-AKVE6WWLYXYx"
class MainActivity : AppCompatActivity() {
    private lateinit var rvRestaurants: RecyclerView
    private val restaurants = mutableListOf<YelpRestaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvRestaurants = findViewById(R.id.rvRestaurants)

        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        //queryData("Avocado Toast")
        handleIntent(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.i(TAG, "Received intent to search for $query")
            //use the query to search your data somehow
            queryData(query.toString())
        }
    }
    private fun queryData(searchTerm: String){
        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create())
                .build()
        val yelpService =retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY", searchTerm, "New York").enqueue(object : Callback<YelpSearchResult>{
            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse $response")
                val body = response.body()
                if (body ==null){
                    Log.w(TAG, "Did not receive valid body from Yelp API... exiting")
                    return
                }
                restaurants.clear()
                restaurants.addAll(body.restaurants)
                Log.i(TAG, "$restaurants")
                rvRestaurants.adapter?.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure $t")
            }
        })
    }
}