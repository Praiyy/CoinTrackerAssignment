package ca.burchill.cointracker.viewModels

import android.app.Application
import androidx.lifecycle.*
import ca.burchill.cointracker.database.getDatabase
import ca.burchill.cointracker.network.CoinApi
import ca.burchill.cointracker.network.CoinApiResponse
import ca.burchill.cointracker.network.NetworkCoin
import ca.burchill.cointracker.repository.CoinsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import java.io.IOException
import java.lang.IllegalArgumentException


enum class CoinApiStatus { LOADING, ERROR, DONE }


class CoinListViewModel(application: Application) : ViewModel() {

    private val coinsRepository = CoinsRepository(getDatabase(application))

    val coins = coinsRepository.coins

    // The internal MutableLiveData that stores the status of the most recent request
    private val _status = MutableLiveData<CoinApiStatus>()
    val status: LiveData<CoinApiStatus>
        get() = _status


/*    private val _coins = MutableLiveData<List<NetworkCoin>>()
    val coins: LiveData<List<NetworkCoin>>
        get() = _coins*/



    // or use viewModelScope
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        refreshDataFromRepository()
        //getCoins()
    }

    private fun refreshDataFromRepository(){
        viewModelScope.launch {
            try {
                coinsRepository.refreshCoins()
                _status.value = CoinApiStatus.DONE
            }catch (networkError: IOException){
                if(coins.value.isNullOrEmpty())
                    _status.value = CoinApiStatus.ERROR
            }
        }
    }

   /* private fun getCoins() {

       coroutineScope.launch {
            try {
                var coinResult = CoinApi.retrofitService.getCoins()
                if (coinResult.coins.size > 0) {
                    _coins.value = coinResult.coins
                }
            } catch (t: Throwable) {
               _status.value = CoinApiStatus.ERROR
            }
        }
    }*/

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app:Application) : ViewModelProvider.Factory{
        override  fun<T : ViewModel?> create(modelClass: Class<T>): T{
            if(modelClass.isAssignableFrom(CoinListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CoinListViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}