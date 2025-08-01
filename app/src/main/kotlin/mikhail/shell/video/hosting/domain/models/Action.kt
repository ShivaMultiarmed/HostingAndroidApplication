package mikhail.shell.video.hosting.domain.models

data class ActionModel<T>(
    val action: Action,
    val model: T
)

enum class Action {
    ADD, REMOVE, UPDATE
}
