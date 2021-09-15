package com.silent.ilustriscore.core.model

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ModelContract
import com.silent.ilustriscore.core.presenter.BasePresenter
import com.silent.ilustriscore.core.utilities.MessageType
import com.silent.ilustriscore.core.utilities.OperationType
import com.silent.ilustriscore.core.utilities.SEARCH_SUFFIX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseModel<T>(private val presenter: BasePresenter<T>) : ModelContract<T>,
    OnCompleteListener<Void>,
    EventListener<QuerySnapshot> where T : BaseBean {

    protected val reference: CollectionReference by lazy {
        FirebaseFirestore.getInstance().collection(path)
    }

    val currentUser: FirebaseUser? by lazy {
        FirebaseAuth.getInstance().currentUser
    }

    private fun saveComplete(data: T): OnCompleteListener<DocumentReference> {
        return OnCompleteListener {
            if (it.isSuccessful) {
                presenter.modelCallBack(
                    successMessage(
                        "Dados salvos com sucesso: $data",
                        OperationType.DATA_SAVED
                    )
                )
            } else {
                presenter.errorCallBack(errorMessage("Ocorreu um erro ao salvar os dados de $data \n ${it.exception?.message} "))
            }
        }
    }

    private fun updateComplete(data: T): OnCompleteListener<Void> {
        return OnCompleteListener {
            if (it.isSuccessful) {
                presenter.modelCallBack(
                        successMessage(
                                "Dados atualizados com sucesso: $data",
                                OperationType.DATA_UPDATED
                        )
                )
            } else {
                presenter.errorCallBack(
                        errorMessage(
                                "Ocorreu um erro ao atualizar os dados de $data \n ${it.exception?.message} ",
                                ErrorType.UPDATE
                        )
                )
            }
        }
    }

    override fun addData(data: T, forcedID: String?) {
        GlobalScope.launch {
            if (forcedID.isNullOrEmpty()) {
                reference.add(data).addOnCompleteListener(saveComplete(data))
            } else {
                editData(data)
            }
        }
    }

    fun uploadToStorage(uri: String, onSuccess: (String) -> Unit) {
        try {
            val file = File(uri)
            val uriFile = Uri.fromFile(file)
            val storageRef = FirebaseStorage.getInstance().reference
            val iconRef = storageRef.child("$path/${file.name}")
            val uploadTask = iconRef.putFile(uriFile)
            uploadTask.addOnFailureListener {
                presenter.errorCallBack(
                    DataException(
                        "Ocorreu um erro ao salvar o arquivo em ${it.message}",
                        ErrorType.SAVE
                    )
                )
            }
            uploadTask.addOnSuccessListener {
                val downloadUrl = it.storage.downloadUrl
                downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
                downloadUrl.addOnFailureListener {
                    presenter.errorCallBack(
                        DataException(
                            "Ocorreu um erro ao salvar o ícone ${it.message}",
                            ErrorType.SAVE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            errorMessage("Ocorreu um erro ao salvar os arquivos ${e.message}", ErrorType.SAVE)
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

    override fun editData(data: T) {
        if (isDisconnected()) return
        Log.i(javaClass.simpleName, "editing: $data")
        reference.document(data.id).set(data).addOnCompleteListener(updateComplete(data))
    }

    fun editField(data: Any, id: String, field: String) {
        if (isDisconnected()) return
        GlobalScope.launch {
            reference.document(id).update(field, data).addOnCompleteListener(this@BaseModel)
        }

    }

    private fun isDisconnected(): Boolean {
        if (currentUser == null) {
            presenter.errorCallBack(DataException(message = "Usuário desconectado", code = ErrorType.DISCONNECTED))
            return true
        }
        return false
    }

    override fun deleteData(id: String) {
        if (isDisconnected()) return
        reference.document(id).delete().addOnCompleteListener(this@BaseModel)
    }

    override fun query(query: String, field: String) {
        if (isDisconnected()) return
        presenter.modelCallBack(infoMessage("Buscando por $query em $field na collection $path"))
        reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX)
                .addSnapshotListener(this)
    }

    fun explicitSearch(query: String, field: String) {
        if (isDisconnected()) return
        presenter.modelCallBack(infoMessage("Buscando por $query em $field na collection $path"))
        reference.whereEqualTo(field, query).addSnapshotListener(this)
    }

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        GlobalScope.launch(Dispatchers.IO) {
            if (error != null) {
                presenter.errorCallBack(errorMessage("Erro ao receber dados ${error.message}"))
                return@launch
            }
            val dataList: ArrayList<T> = ArrayList()
            for (doc in value!!) {
                deserializeDataSnapshot(doc).let { dataList.add(it) }
            }
            presenter.modelCallBack(
                    successMessage(
                            "Dados recebidos: $dataList",
                            OperationType.DATA_RETRIEVED
                    )
            )
            presenter.onDataRetrieve(dataList)
        }
    }

    override fun getAllData() {
        if (isDisconnected()) return
        GlobalScope.launch(Dispatchers.IO) {
            reference.addSnapshotListener(this@BaseModel)
        }
    }

    override fun getSingleData(id: String) {
        if (isDisconnected()) return
        GlobalScope.launch(Dispatchers.IO) {
            Log.i(javaClass.name, "querying data $id")
            reference.document(id).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    presenter.errorCallBack(
                            errorMessage(
                                    e.message
                                            ?: "Ocorreu um erro ao obter dados de $id", ErrorType.NOT_FOUND)
                    )
                }
                if (snapshot != null && snapshot.exists()) {
                    deserializeDataSnapshot(snapshot).let { presenter.onSingleData(it) }
                } else {
                    presenter.errorCallBack(
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
            presenter.modelCallBack(DTOMessage("Operação concluída", MessageType.SUCCESS))
        } else {
            presenter.errorCallBack(
                    errorMessage("Ocorreu um erro ao processar\n->${task.exception?.message}")
            )
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
                presenter.errorCallBack(
                        errorMessage(
                                "Ocorreu um erro ${e.message}",
                                ErrorType.DELETE
                        )
                )
            }
        }
    }

}
