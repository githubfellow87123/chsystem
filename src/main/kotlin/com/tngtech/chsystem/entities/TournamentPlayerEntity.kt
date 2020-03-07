package com.tngtech.chsystem.entities

import javax.persistence.*

@Entity
@Table(name = "TOURNAMENT_PLAYERS")
data class TournamentPlayerEntity(
    @EmbeddedId
    val tournamentPlayerId: TournamentPlayerId,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tournamentId")
    @JoinColumn(name = "TOURNAMENT_ID", columnDefinition = "UUID")
    val tournamentEntity: TournamentEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playerId")
    @JoinColumn(name = "PLAYER_ID", columnDefinition = "UUID")
    val playerEntity: PlayerEntity,
    @Column(nullable = true)
    val rank: Int?,
    @Column(nullable = true)
    val seatingOrder: Int?
) {
    constructor(tournamentEntity: TournamentEntity, playerEntity: PlayerEntity) : this(
        TournamentPlayerId(
            tournamentEntity.id,
            playerEntity.id
        ),
        tournamentEntity,
        playerEntity,
        null,
        null
    )

    override fun toString(): String = "TournamentPlayerEntity[" +
            "tournamentId = ${tournamentPlayerId.tournamentId}, " +
            "playerId = ${tournamentPlayerId.playerId}, " +
            "rank: $rank, " +
            "seatingOrder: $seatingOrder" +
            "]"

    override fun hashCode(): Int {
        return tournamentPlayerId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return (other is TournamentPlayerEntity)
                && tournamentPlayerId == other.tournamentPlayerId
                && rank == other.rank
                && seatingOrder == other.seatingOrder
    }
}