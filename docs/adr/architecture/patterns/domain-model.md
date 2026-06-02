# Pattern: Domain Model

## Description

Domain Models represent the business objects and rules. They have no dependencies on frameworks or infrastructure.

## Entities

Have a unique identity and a lifecycle.

```kotlin
data class User(
    val id: UserId,
    val name: String,
    val email: Email,
    val roles: Set<Role>,
    val createdAt: Instant
) {
    fun hasRole(role: Role): Boolean = role in roles

    fun assignRole(role: Role): User = copy(roles = roles + role)

    companion object {
        fun create(name: String, email: Email): User = User(
            id = UserId.generate(),
            name = name,
            email = email,
            roles = emptySet(),
            createdAt = Instant.now()
        )
    }
}
```

## Value Objects

Defined by their attributes, not by an identity. Immutable.

```kotlin
@JvmInline
value class UserId(val value: String) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.contains("@")) { "Invalid email: $value" }
    }
}
```

## Rules

1. Domain Models are **immutable** (Kotlin `data class` / `value class`)
2. Business rules live **inside** Domain Models (not in Services)
3. No framework annotations (`@Entity`, `@JsonProperty`, etc.)
4. Validation in the constructor / `init` block
5. Factory methods for complex creation (`companion object`)
