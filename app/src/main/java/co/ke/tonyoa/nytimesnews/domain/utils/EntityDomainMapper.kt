package co.ke.tonyoa.nytimesnews.domain.utils

abstract class EntityDomainMapper<Entity, Domain> {
    abstract fun entityToDomain(entity: Entity): Domain
    abstract fun domainToEntity(domain: Domain): Entity

    fun entityListToDomainList(entities: List<Entity>): List<Domain> {
        return entities.map { entityToDomain(it) }
    }

    fun domainListToEntityList(domains: List<Domain>): List<Entity> {
        return domains.map { domainToEntity(it) }
    }
}