package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.score.ScoreService
import org.springframework.stereotype.Service
import java.util.*

@Service
class RankingService(
    private val scoreService: ScoreService,
    private val random: Random,
    private val matchService: MatchService
) {
    fun rankPlayers(tournament: TournamentEntity): List<PlayerEntity>? {
        val playedMatches = matchService.convertToPlayedMatches(tournament.matches)
            ?: return null
        val playerToMatches = matchService.mapPlayersToMatches(tournament.getPlayers(), playedMatches)
        return rankPlayers(playerToMatches)
    }

    fun rankPlayers(playerToMatches: Map<PlayerEntity, Set<PlayedMatch>>): List<PlayerEntity> {

        val playerToScore = scoreService.calculatePlayerScores(playerToMatches)

        return sortPlayers(playerToScore)
    }

    private fun sortPlayers(playerToScore: Map<PlayerEntity, Score>): List<PlayerEntity> {
        val sortedPlayers = playerToScore.keys.toList()
            .sortedByDescending { playerToScore.getValue(it) }

        return shufflePlayersWithEqualScore(sortedPlayers, playerToScore)
    }

    private fun shufflePlayersWithEqualScore(
        sortedPlayers: List<PlayerEntity>,
        playerToScore: Map<PlayerEntity, Score>
    ): List<PlayerEntity> {
        val sortedPlayersEqualScoreShuffled = sortedPlayers.toMutableList()

        var i = 0
        while (i < sortedPlayersEqualScoreShuffled.size) {

            val playerI = sortedPlayersEqualScoreShuffled[i]
            val scorePlayerI = playerToScore.getValue(playerI)
            var j = i + 1

            while (j < sortedPlayersEqualScoreShuffled.size) {

                val playerJ = sortedPlayersEqualScoreShuffled[j]
                val scorePlayerJ = playerToScore.getValue(playerJ)

                if (scorePlayerI != scorePlayerJ) {
                    break
                }

                j++
            }

            sortedPlayersEqualScoreShuffled.subList(i, j).shuffle(random)
            i = j
        }

        return sortedPlayersEqualScoreShuffled
    }
}