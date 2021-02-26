package darwin

import org.springframework.http.HttpStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.web.bind.annotation.PostMapping
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping
import io.swagger.annotations.ApiModel
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Service
import org.springframework.http.MediaType
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.annotation.Id
import org.springframework.dao.DuplicateKeyException

@ApiModel
@Document(collection = "assistant")
data class Assistant(
  val apiKey: String,
  @Indexed(unique = true) val instance: String,
  @JsonIgnore @Id val idAssistant: String? = null
)

fun Assistant(key: String = java.util.UUID.randomUUID().toString()): Assistant =
  Assistant(instance = key, apiKey = java.util.Base64.getEncoder()
    .encodeToString(java.util.Calendar.getInstance().getTimeInMillis()
      .toString().toByteArray()))

interface AssistantRepository: MongoRepository<Assistant, String> {
  fun findByInstance(instance: String): Assistant
  fun deleteByInstance(instance: String): Long
}

data class AssistantException(val message_: String,
  val statusCode: HttpStatus): IllegalArgumentException(message_)

data class AssistantErrorResponse(val message: String,
    @JsonIgnore val statusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) {
  companion object {
    val DEFAULT_ERROR_MESSAGE = "Unexpected error."
  }
}

@Service
open class AssistantService(
  @Autowired val assistantRepository: AssistantRepository
) {

  companion object {
    val LOGGER = LoggerFactory.getLogger(AssistantService::class.java)
    val NO_INSTANCE_FOUND_MESSAGE = "No assistant found with given instance identifier: "
  }

  fun new(instance: String?) = try {
    this.assistantRepository.insert(instance?.let { Assistant(it) } ?: run { Assistant() })
  }
  catch(ex: DuplicateKeyException) {
    ex.message?.let { message ->
      if(message.contains("duplicate key error collection")) throw AssistantException(
        "Instance were taken, please use another one.", HttpStatus.BAD_REQUEST)
    }
    throw ex
  }

  fun findByInstance(instance: String) = try {
    this.assistantRepository.findByInstance(instance)
  }
  catch(ex: EmptyResultDataAccessException) {
    LOGGER.info(NO_INSTANCE_FOUND_MESSAGE + instance)
    throw AssistantException(NO_INSTANCE_FOUND_MESSAGE, HttpStatus.BAD_REQUEST)
  }

  fun deleteByInstance(instance: String) =
    this.assistantRepository.deleteByInstance(instance) > 0

}

@RestController @RequestMapping(
  path = arrayOf("/api/v1/instance"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class InstanceController(
  @Autowired val assistantService: AssistantService
) {

  fun errorHandle(error: Exception) =
    if(error is AssistantException)
      ResponseEntity(AssistantErrorResponse(error.message_), error.statusCode)
    else {
      val response = AssistantErrorResponse(
        error.message?.let { it } ?: run { AssistantErrorResponse.DEFAULT_ERROR_MESSAGE })
      ResponseEntity(response, response.statusCode)
    }

  @PostMapping
  @ApiOperation(value = "Create instance from given key (or leave it blank for auto).")
  fun new(@ApiParam(value = "Key used as instance identifier")
      @RequestParam(required = false) instance: String?) =
    try { this.assistantService.new(instance) }
    catch(e: Exception) { this.errorHandle(e) }

  @GetMapping("/{instance}")
  @ApiOperation(value = "Find instance from given key.")
  fun find(@PathVariable instance: String) =
    try { this.assistantService.findByInstance(instance) }
    catch(ex: AssistantException) { this.errorHandle(ex) }

  @DeleteMapping("/{instance}")
  @ApiOperation(value = "Delete instance from given key.")
  fun delete(@PathVariable instance: String) =
    this.assistantService.deleteByInstance(instance)

}
