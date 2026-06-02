# Pattern: Application Services

## Description

Application Services orchestrate Domain Services and implement Use Cases. They are the bridge between Driving Adapters and the Domain.

## Structure

```kotlin
class ManageApplicationService(
    private val applicationRepository: ApplicationRepository,  // Outbound Port
    private val oidcProvider: OidcProviderGateway,             // Outbound Port
    private val eventPublisher: DomainEventPublisher            // Outbound Port
) : CreateApplicationUseCase, UpdateApplicationUseCase {

    override suspend fun create(command: CreateApplicationCommand): Application {
        // 1. Execute domain logic
        val application = Application.create(command.name, command.owner)

        // 2. Persist via Outbound Port
        val saved = applicationRepository.save(application)

        // 3. Publish event
        eventPublisher.publish(ApplicationCreatedEvent(saved.id))

        return saved
    }
}
```

## Rules

1. Application Services implement **Inbound Ports**
2. They use **Outbound Ports** for infrastructure access
3. No business logic — that belongs in Domain Services / Models
4. Transaction management at the Application Service level
5. An Application Service can implement multiple Use Cases
6. Commands and Queries as dedicated data classes (CQRS-light)

## Command / Query Pattern

```kotlin
// Command (write)
data class CreateApplicationCommand(
    val name: String,
    val owner: UserId
)

// Query (read)
data class FindApplicationQuery(
    val id: ApplicationId
)
```
