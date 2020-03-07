package com.tngtech.chsystem.service.score

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.StatisticScore
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StatisticScoreService(
    private val matchService: MatchService
) {

    fun calculateScores(tournament: TournamentEntity): Map<PlayerEntity, StatisticScore> {
        val playedMatches = matchService.getAllPlayedMatches(tournament.matches)
        val playerToMatches = matchService.mapPlayersToMatches(tournament.getPlayers(), playedMatches)
        return calculateScores(playerToMatches)
    }

    private fun calculateScores(playerToMatches: Map<PlayerEntity, Set<PlayedMatch>>): Map<PlayerEntity, StatisticScore> {
        val playerToStatisticScore = HashMap<PlayerEntity, StatisticScore>()

        for ((player, matches) in playerToMatches) {

            val matchWins = calculateMatchWins(player, matches)
            val matchLosses = calculateMatchLosses(player, matches)
            val matchDraws = calculateMatchDraws(player, matches)
            val gameWins = calculateGameWins(player, matches)
            val gameLosses = calculateGameLosses(player, matches)
            val latestMatchUpdate = calculateLatestMatchUpdate(player, matches)

            playerToStatisticScore[player] = StatisticScore(
                matchWins = matchWins,
                matchLosses = matchLosses,
                matchDraws = matchDraws,
                gameWins = gameWins,
                gameLosses = gameLosses,
                latestMatchUpdate = latestMatchUpdate
            )
        }

        return playerToStatisticScore
    }

    private fun calculateMatchWins(player: PlayerEntity, matches: Set<PlayedMatch>): Int {
        var matchWins = 0

        for (match in matches) {
            when {
                match.player1 == player -> if (match.winsPlayer1 > match.winsPlayer2) matchWins += 1
                match.player2 == player -> if (match.winsPlayer2 > match.winsPlayer1) matchWins += 1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return matchWins
    }

    private fun calculateMatchLosses(player: PlayerEntity, matches: Set<PlayedMatch>): Int {
        var matchLosses = 0

        for (match in matches) {
            when {
                match.player1 == player -> if (match.winsPlayer1 < match.winsPlayer2) matchLosses += 1
                match.player2 == player -> if (match.winsPlayer2 < match.winsPlayer1) matchLosses += 1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return matchLosses
    }

    private fun calculateMatchDraws(player: PlayerEntity, matches: Set<PlayedMatch>): Int {
        var matchDraws = 0

        for (match in matches) {
            when {
                match.player1 == player || match.player2 == player -> if (match.winsPlayer1 == match.winsPlayer2) matchDraws += 1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return matchDraws
    }

    private fun calculateGameWins(player: PlayerEntity, matches: Set<PlayedMatch>): Int {
        var gameWins = 0

        for (match in matches) {
            gameWins += when {
                match.player1 == player -> match.winsPlayer1
                match.player2 == player -> match.winsPlayer2
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return gameWins
    }

    private fun calculateGameLosses(player: PlayerEntity, matches: Set<PlayedMatch>): Int {
        var gameLosses = 0

        for (match in matches) {
            gameLosses += when {
                match.player1 == player -> match.winsPlayer2
                match.player2 == player -> match.winsPlayer1
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return gameLosses
    }

    private fun calculateLatestMatchUpdate(player: PlayerEntity, matches: Set<PlayedMatch>): LocalDateTime? {
        var latestMatchUpdate: LocalDateTime? = null

        for (match in matches) {
            when {
                match.player1 == player || match.player2 == player -> if (latestMatchUpdate == null || latestMatchUpdate < match.lastUpdated) {
                    latestMatchUpdate = match.lastUpdated
                }
                else -> throw RuntimeException("Match $match does not belong to player $player")
            }
        }
        return latestMatchUpdate
    }
}