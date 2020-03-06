package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.dto.RankingScore
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.score.RankingScoreService
import org.springframework.stereotype.Service
import java.util.*

@Service
class RankingService(
    private val rankingScoreService: RankingScoreService,
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

        val playerToScore = rankingScoreService.calculatePlayerScores(playerToMatches)

        return sortPlayers(playerToScore)
    }

    private fun sortPlayers(playerToRankingScore: Map<PlayerEntity, RankingScore>): List<PlayerEntity> {
        val sortedPlayers = playerToRankingScore.keys.toList()
            .sortedByDescending { playerToRankingScore.getValue(it) }

        return shufflePlayersWithEqualScore(sortedPlayers, playerToRankingScore)
    }

    private fun shufflePlayersWithEqualScore(
        sortedPlayers: List<PlayerEntity>,
        playerToRankingScore: Map<PlayerEntity, RankingScore>
    ): List<PlayerEntity> {
        val sortedPlayersEqualScoreShuffled = sortedPlayers.toMutableList()

        var i = 0
        while (i < sortedPlayersEqualScoreShuffled.size) {

            val playerI = sortedPlayersEqualScoreShuffled[i]
            val scorePlayerI = playerToRankingScore.getValue(playerI)
            var j = i + 1

            while (j < sortedPlayersEqualScoreShuffled.size) {

                val playerJ = sortedPlayersEqualScoreShuffled[j]
                val scorePlayerJ = playerToRankingScore.getValue(playerJ)

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