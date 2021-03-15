package com.silent.ilustriscore.core.model

import android.util.Log
import com.creat.motiv.utilities.SEARCH_SUFFIX
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.silent.ilustriscore.core.bean.BaseBean
import com.silent.ilustriscore.core.contract.ModelContract
import com.silent.ilustriscore.core.presenter.BasePresenter
import com.silent.ilustriscore.core.utilities.MessageType
import com.silent.ilustriscore.core.utilities.OperationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
                throw DataException("Erro aos salvar dados em $path", ErrorType.NOT_FOUND)
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
                throw DataException("Erro aos salvar dados em $path", ErrorType.UPDATE)
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


    private fun successMessage(
        message: String = "Operação concluída com sucesso",
        operationType: OperationType
    ): DTOMessage = DTOMessage(message, MessageType.SUCCESS, operationType = operationType)

    fun warningMessage(message: String = "Um erro inesperado aconteceu, recomenda-se verificar"): DTOMessage =
        DTOMessage(message, MessageType.WARNING)

    private fun infoMessage(message: String): DTOMessage = DTOMessage(message, MessageType.INFO)

    override fun editData(data: T) {
        isDisconnected()
        Log.i(javaClass.simpleName, "editing: $data")
        reference.document(data.id).set(data).addOnCompleteListener(updateComplete(data))
    }

    fun editField(data: Any, id: String, field: String) {
        isDisconnected()
        GlobalScope.launch {
            reference.document(id).update(field, data).addOnCompleteListener(this@BaseModel)
        }

    }

    private fun isDisconnected() {
        if (currentUser == null) {
            throw DataException("Usuário desconectado", ErrorType.DISCONNECTED)
        }
    }

    override fun deleteData(id: String) {
        isDisconnected()
        reference.document(id).delete().addOnCompleteListener(this@BaseModel)
    }

    override fun query(query: String, field: String) {
        isDisconnected()
        presenter.modelCallBack(infoMessage("Buscando por $query em $field na collection $path"))
        reference.orderBy(field).startAt(query).endAt(query + SEARCH_SUFFIX).addSnapshotListener(this)
    }

    fun explicitSearch(query: String, field: String) {
        isDisconnected()
        presenter.modelCallBack(infoMessage("Buscando por $query em $field na collection $path"))
        reference.whereEqualTo(field, query).addSnapshotListener(this)
    }

    private fun handleDataExceptionError(error: FirebaseFirestoreException) {
        throw DataException("Ocorreu um erro ao obter os dados $error", ErrorType.UNKNOWN)

    }

    override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
        GlobalScope.launch(Dispatchers.IO) {
            error?.let {
                handleDataExceptionError(it)
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
        isDisconnected()
        GlobalScope.launch(Dispatchers.IO) {
            reference.addSnapshotListener(this@BaseModel)
        }
    }

    override fun getSingleData(id: String) {
        isDisconnected()
        GlobalScope.launch(Dispatchers.IO) {
            Log.i(javaClass.name, "querying data $id")
            reference.document(id).addSnapshotListener { snapshot, error ->
                error?.let {
                    handleDataExceptionError(it)
                }
                if (snapshot != null && snapshot.exists()) {
                    deserializeDataSnapshot(snapshot).let { presenter.onSingleData(it) }
                } else {
                    throw DataException("Erro ao encontrar $id em $path", ErrorType.NOT_FOUND)
                }
            }
        }

    }


    override fun onComplete(task: Task<Void>) {
        if (task.isSuccessful) {
            presenter.modelCallBack(DTOMessage("Operação concluída", MessageType.SUCCESS))
        } else {
            throw DataException("Ocorreu um erro ao processar\n->${task.exception?.message}")

        }
    }

    fun deleteAllData(dataList: List<T>) {
        isDisconnected()
        GlobalScope.launch {
            try {
                for (data in dataList) {
                    if (data.id.isNotEmpty()) {
                        deleteData(data.id)
                    }
                }
            } catch (e: Exception) {
                throw DataException("Ocorreu um erro ao deletar os dados $e", ErrorType.UPDATE)
            }
        }
    }

}
