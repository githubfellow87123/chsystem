package com.tngtech.chsystem.service.matchmaking

import com.tngtech.chsystem.dao.MatchRepository
import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.service.rank.PlayerMatchesService
import com.tngtech.chsystem.service.rank.RankingService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class MatchmakingService(
    private val playerMatchesService: PlayerMatchesService,
    private val rankingService: RankingService,
    private val matchRepository: MatchRepository,
    private val pairingService: PairingService
) {

    fun generateMatchesForNextRound(tournament: TournamentEntity): Set<MatchEntity>? {

        val playedMatches: Set<PlayedMatch> = tournament.matches.stream()
            .map { matchEntity -> matchEntity.toPlayedMatch() }
            .collect(Collectors.toSet())
        val playerToMatches = playerMatchesService.mapPlayersToMatches(tournament.players, playedMatches)
        val rankedPlayers = rankingService.rankPlayers(playerToMatches)
        val pairings = pairingService.generatePairingsForNextRound(rankedPlayers, playerToMatches) ?: return null
        val matches = pairings.stream()
            .map { pair ->
                MatchEntity(
                    tournament = tournament,
                    roundIndex = tournament.roundIndex + 1,
                    player1 = rankedPlayers[pair.first],
                    // The rank rankedPlayers.size is only used for the bye
                    player2 = if (pair.second == rankedPlayers.size) null else rankedPlayers[pair.second]
                )
            }
            .collect(Collectors.toSet())

        matchRepository.saveAll(matches)

        return matches
    }

    private fun MatchEntity.toPlayedMatch(): PlayedMatch {
        if (winsPlayer1 == null || winsPlayer2 == null) {
            throw RuntimeException("Match $this is was not played, results are missing")
        }

        return PlayedMatch(
            id,
            tournament,
            roundIndex,
            player1,
            player2,
            winsPlayer1!!,
            winsPlayer2!!
        )
    }
}