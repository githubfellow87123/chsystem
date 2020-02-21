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

    fun generateMatchesForNextRound(tournament: TournamentEntity): MatchmakingCode {

        val playedMatches = HashSet<PlayedMatch>()
        for (match in tournament.matches) {
            val playedMatch = match.toPlayedMatch()
                ?: return MatchmakingCode.MISSING_RESULTS_OF_CURRENT_ROUND
            playedMatches.add(playedMatch)
        }

        val playerToMatches = playerMatchesService.mapPlayersToMatches(tournament.players, playedMatches)
        val rankedPlayers = rankingService.rankPlayers(playerToMatches)
        val pairings = pairingService.generatePairingsForNextRound(rankedPlayers, playerToMatches)
            ?: return MatchmakingCode.NO_VALID_MATCHES_FOR_NEXT_ROUND_AVAILABLE
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

        return MatchmakingCode.SUCCESSFUL
    }

    private fun MatchEntity.toPlayedMatch(): PlayedMatch? {
        val winsPlayer1Val = winsPlayer1
        val winsPlayer2Val = winsPlayer2
        if (winsPlayer1Val == null || winsPlayer2Val == null) {
            return null
        }

        return PlayedMatch(
            id,
            tournament,
            roundIndex,
            player1,
            player2,
            winsPlayer1Val,
            winsPlayer2Val
        )
    }
}