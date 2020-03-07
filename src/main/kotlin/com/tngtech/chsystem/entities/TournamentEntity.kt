package com.tngtech.chsystem.entities

import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors
import javax.persistence.*
import kotlin.collections.HashSet

@Entity
@Table(name = "TOURNAMENT")
data class TournamentEntity(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val state: TournamentState = TournamentState.INITIALIZING,
    @Column
    val roundIndex: Int = 0,
    @OneToMany(mappedBy = "tournamentEntity", cascade = [CascadeType.ALL], orphanRemoval = true)
    val tournamentPlayers: MutableSet<TournamentPlayerEntity> = HashSet()
) {
    @OneToMany(mappedBy = "tournament")
    val matches: MutableSet<MatchEntity> = HashSet()

    fun getPlayers(): Set<PlayerEntity> {
        return tournamentPlayers.stream().map { p -> p.playerEntity }.collect(Collectors.toSet())
    }

    fun addPlayer(player: PlayerEntity) {
        val tournamentPlayerEntity = TournamentPlayerEntity(this, player)
        tournamentPlayers.add(tournamentPlayerEntity)
    }

    fun removePlayer(player: PlayerEntity) {
        val tournamentPlayerEntity = TournamentPlayerEntity(this, player)
        tournamentPlayers.remove(tournamentPlayerEntity)
    }

    fun setRankOfPlayer(player: PlayerEntity, rank: Int): Boolean {
        val tournamentPlayer =
            tournamentPlayers.find { tournamentPlayer -> tournamentPlayer.playerEntity == player }
                ?: return false

        tournamentPlayers.remove(tournamentPlayer)
        tournamentPlayers.add(tournamentPlayer.copy(rank = rank))

        return true
    }

    fun getRankOfPlayer(player: PlayerEntity): Int? {
        val tournamentPlayer =
            tournamentPlayers.find { tournamentPlayer -> tournamentPlayer.playerEntity == player }
                ?: return null

        return tournamentPlayer.rank
    }

    fun getRankOfPlayer(playerName: String): Int? {
        val tournamentPlayer =
            tournamentPlayers.find { tournamentPlayer -> tournamentPlayer.playerEntity.name == playerName }
                ?: return null

        return tournamentPlayer.rank
    }
}