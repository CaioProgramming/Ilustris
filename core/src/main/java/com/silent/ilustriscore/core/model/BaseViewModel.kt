package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ModelContract
import com.silent.ilustriscore.core.utilities.MessageType
import com.silent.ilustriscore.core.utilities.OperationType
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseViewModel<T> : ViewModel(), ModelContract<T>,
    OnCompleteListener<Void>,
    EventListener<QuerySnapshot> where T : BaseBean {


    val viewModelState = MutableLiveData<ViewModelBaseState>()

    var requireAuth: Boolean = true

    val reference: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection(dataPath)
    }

    val currentUser: FirebaseUser? by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    private fun saveComplete(data: T): OnCompleteListener<DocumentReference> {
        return OnCompleteListener {
            if (it.isSuccessful) {
                viewModelState.postValue(ViewModelBaseState.DataSavedState(data))
            } else {
                updateBaseState(ViewModelBaseState.ErrorState(errorMessage("Ocorreu um erro ao salvar os dados de $data \n ${it.exception?.message} ")))
            }
        }
    }

    private fun updateComplete(data: T): OnCompleteListener<Void> {
        return OnCompleteListener {
            if (it.isSuccessful) {
                Log.d(javaClass.simpleName, "updateComplete: Dados atualizados com sucesso: $data")
                viewModelState.postValue(ViewModelBaseState.DataUpdateState(data))

            } else {
                updateBaseState(
                    ViewModelBaseState.ErrorState(
                        errorMessage(
                            "Ocorreu um erro ao atualizar os dados de $data \n ${it.exception?.message} ",
                            ErrorType.UPDATE
                        )
                    )
                )
            }
        }
    }

    private fun updateBaseState(viewModelBaseState: ViewModelBaseState) {
        viewModelState.postValue(viewModelBaseState)
    }

    protected fun addData(data: T, forcedID: String?) {
        GlobalScope.launch {
            if (forcedID.isNullOrEmpty()) {
                reference.add(data).addOnCompleteListener(saveComplete(data))
            } else {
                editData(data)
            }
        }
    }

    fun dispatchViewAction(baseActions: ViewModelBaseActions) {
        when (baseActions) {
            is ViewModelBaseActions.DeleteDataAction -> deleteData(baseActions.id)
            ViewModelBaseActions.GetAllDataAction -> getAllData()
            is ViewModelBaseActions.GetDataByIdAction -> getSingleData(baseActions.id)
            is ViewModelBaseActions.PreciseQueryDataAction -> explicitSearch(
                baseActions.value,
                baseActions.field
            )
            is ViewModelBaseActions.QueryDataAction -> query(baseActions.value, baseActions.field)
            is ViewModelBaseActions.SaveDataAction -> addData(baseActions.data as T, null)
            is ViewModelBaseActions.UpdateDataAction -> editData(baseActions.data as T)
            is ViewModelBaseActions.UploadFileAction -> uploadToStorage(baseActions.uri)
            is ViewModelBaseActions.SavePreciseDataAction -> addData(
                baseActions.data as T,
                baseActions.forcedID
            )
        }
    }

    fun uploadToStorage(uri: String) {
        try {
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val storageRef = FirebaseStorage.getInstance().reference
            val iconRef = storageRef.child("$dataPath/${file.name}")
            val uploadTask = iconRef.putFile(uriFile)
            uploadTask.addOnFailureListener {
                updateBaseState(
                    ViewModelBaseState.ErrorState(
                        DataException(
                            "Ocorreu um erro ao salvar o arquivo em ${it.message}",
                            ErrorType.SAVE
                        )
                    )
                )
            }
            uploadTask.addOnSuccessListener {
                val downloadUrl = it.storage.downloadUrl
                downloadUrl.addOnSuccessListener { result ->
                    updateBaseState(ViewModelBaseState.FileUploadedState(result))
                }
                downloadUrl.addOnFailureListener { exception ->

                    updateBaseState(
                        ViewModelBaseState.ErrorState(
                            DataException(
                                "Ocorreu um erro ao salvar o ícone ${exception.message}",
                                ErrorType.SAVE
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            updateBaseState(
                ViewModelBaseState.ErrorState(
                    errorMessage(
                        "Ocorreu um erro ao salvar os arquivos ${e.message}",
                        ErrorType.SAVE
                    )
                )
            )
        }
    }

    private fun errorMessage(
        message: String = "Ocorreu um erro ao processar",
        errorType: ErrorType = ErrorType.UNKNOWN
    ): DataException = DataException(message, errorType)

    private fun successMessage(
        message: String = "Operação concluída com sucesso",
        operationType: OperationType
    ): DTOMessage = DTOMessage(message, MessageType.SUCCESS, operationType = operationType)

    fun warningMessage(message: String = "Um erro inesperado aconteceu, recomenda-se verificar"): DTOMessage =
        DTOMessage(message, MessageType.WARNING)

    private fun infoMessage(message: String): DTOMessage = DTOMessage(message, MessageType.INFO)

    protected fun editData(data: T) {
        if (isDisconnected()) return
        Log.i(javaClass.simpleName, "editing: $data")
        reference.document(data.id).set(data).addOnCompleteListener(updateComplete(data))
    }

    fun editField(data: Any, id: String, field: String) {
        if (isDisconnected()) return
        GlobalScope.launch {
            reference.document(id).update(field, data).addOnCompleteListener(this@BaseViewModel)
        }

    }

    private fun isDisconnected(): Boolean {
        if (currentUser == null && requireAuth) {
            updateBaseState(
                ViewModelBaseState.ErrorState(
                    DataException(
                        message = "Usuário desconectado",
                        code = ErrorType.DISCONNECTED
                    )
                )
            )
            return true
        }
        return false
    }

    protected fun deleteData(id: String) {
        if (isDisconnected()) return
        reference.document(id).delete().addOnCompleteListener(this@BaseViewModel)
    }

    protected fun query(query: String, field: String) {
        if (isDisconnected()) return
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX)
            .addSnapshotListener(this)
    }

    fun explicitSearch(query: String, field: String) {
        if (isDisconnected()) return
        Log.i(javaClass.simpleName, "query: Buscando por $query em $field na collection $dataPath")
        reference.whereEqualTo(field, query).addSnapshotListener(this)
    }

    private fun sendErrorState(dataException: DataException) {
        updateBaseState(ViewModelBaseState.ErrorState(dataException))
    }

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        GlobalScope.launch(Dispatchers.IO) {
            if (error != null) {
                sendErrorState(errorMessage("Erro ao receber dados ${error.message}"))
                return@launch
            }
            val dataList: ArrayList<T> = ArrayList()
            for (doc in value!!) {
                deserializeDataSnapshot(doc).let { dataList.add(it) }
            }
            Log.d(javaClass.simpleName, "onEvent: Dados recebidos: $dataList")
            updateBaseState(ViewModelBaseState.DataListRetrievedState(dataList))
        }
    }

    protected fun getAllData() {
        if (isDisconnected()) return
        GlobalScope.launch(Dispatchers.IO) {
            reference.addSnapshotListener(this@BaseViewModel)
        }
    }

    protected fun getSingleData(id: String) {
        if (isDisconnected()) return
        GlobalScope.launch(Dispatchers.IO) {
            Log.i(javaClass.name, "querying data $id")
            reference.document(id).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    sendErrorState(
                        errorMessage(
                            e.message
                                ?: "Ocorreu um erro ao obter dados de $id", ErrorType.NOT_FOUND
                        )
                    )
                }
                if (snapshot != null && snapshot.exists()) {
                    deserializeDataSnapshot(snapshot).let {
                        updateBaseState(ViewModelBaseState.DataRetrievedState(it))
                    }
                } else {
                    sendErrorState(
                        errorMessage(
                            "Dados não encontrados para $id",
                            ErrorType.NOT_FOUND
                        )
                    )
                }
            }
        }

    }


    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            Log.d(javaClass.simpleName, "onComplete: Operação concluída")
        } else {
            sendErrorState(errorMessage("Ocorreu um erro ao processar\n->${task.exception?.message}"))
        }
    }

    fun deleteAllData(dataList: List<T>) {
        if (isDisconnected()) return
        GlobalScope.launch {
            try {
                for (data in dataList) {
                    if (data.id.isNotEmpty()) {
                        deleteData(data.id)
                    }
                }
            } catch (e: Exception) {
                sendErrorState(
                    errorMessage(
                        "Ocorreu um erro ${e.message}",
                        ErrorType.DELETE
                    )
                )

            }
        }
    }

}
