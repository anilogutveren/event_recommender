# Pattern: Ports & Adapters

## Description

Ports define the interfaces between the Application Core and the outside world. Adapters implement these interfaces.

## Inbound Ports (Use Cases)

Define what the application can do — called by Driving Adapters.

```kotlin
// Inbound Port
interface CreateUserUseCase {
    suspend fun execute(command: CreateUserCommand): User
}

// Application Service implements the port
class CreateUserService(
    private val userRepository: UserRepository  // Outbound Port
) : CreateUserUseCase {
    override suspend fun execute(command: CreateUserCommand): User {
        val user = User.create(command.name, command.email)
        return userRepository.save(user)
    }
}
```

## Outbound Ports (Repositories / Gateways)

Define what the application needs from the infrastructure — implemented by Driven Adapters.

```kotlin
// Outbound Port
interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findById(id: UserId): User?
}

// Driven Adapter implements the port
class JpaUserRepository(
    private val jpaRepository: JpaUserEntityRepository
) : UserRepository {
    override suspend fun save(user: User): User {
        val entity = UserEntity.fromDomain(user)
        return jpaRepository.save(entity).toDomain()
    }
}
```

## Rules

1. Ports are **Interfaces** in the `application/port/` package
2. Inbound Ports are implemented by Application Services
3. Outbound Ports are implemented by Adapters
4. Domain models flow through Ports — **no** persistence entities on Ports
5. Adapter-specific annotations (`@RestController`, `@Entity`) only in Adapters
