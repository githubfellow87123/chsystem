package com.tngtech.chsystem.service.matchmaking

import com.tngtech.chsystem.dto.Pairings
import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class PairingService {

    fun generatePairingsForNextRound(
        rankedPlayers: List<PlayerEntity>,
        playerToMatches: Map<PlayerEntity, Set<PlayedMatch>>
    ): List<Pair<Int, Int>>? {

        val pairings = Pairings()

        var rankOfPlayerSearchingForOpponent = 0
        var startingRankOfPossibleOpponent = 1
        while (pairings.getNumberAlreadyAssignedPlayers() < rankedPlayers.size) {

            if (pairings.getRanksOfAlreadyAssignedPlayers().contains(rankOfPlayerSearchingForOpponent)) {
                rankOfPlayerSearchingForOpponent++
                startingRankOfPossibleOpponent = rankOfPlayerSearchingForOpponent + 1
            } else {
                val pairing = assignOpponent(
                    rankOfPlayerSearchingForOpponent,
                    startingRankOfPossibleOpponent,
                    pairings.getRanksOfAlreadyAssignedPlayers(),
                    rankedPlayers,
                    playerToMatches
                )

                if (pairing == null) {
                    val lastPairing = pairings.getLastPairing() ?: return null
                    pairings.removeLastPairing()
                    rankOfPlayerSearchingForOpponent = lastPairing.first
                    startingRankOfPossibleOpponent = lastPairing.second + 1
                } else {
                    val addedPairingSuccessful = pairings.addPairing(pairing.first, pairing.second)
                    if (!addedPairingSuccessful) {
                        throw RuntimeException("Was not able to pair rank ${pairing.first} with ${pairing.second}")
                    }
                }
            }
        }

        return pairings.getListOfPairs()
    }

    internal fun assignOpponent(
        rankOfPlayerSearchingForOpponent: Int,
        startingRankOfPossibleOpponent: Int,
        ranksOfAlreadyAssignedPlayers: Set<Int>,
        rankedPlayers: List<PlayerEntity>,
        playerToMatches: Map<PlayerEntity, Set<PlayedMatch>>
    ): Pair<Int, Int>? {
        val playerSearchingForOpponent = rankedPlayers[rankOfPlayerSearchingForOpponent]

        for (rankOpponent in startingRankOfPossibleOpponent until rankedPlayers.size) {
            val opponent = rankedPlayers[rankOpponent]

            if (!ranksOfAlreadyAssignedPlayers.contains(rankOpponent) &&
                !playedVsAnother(playerSearchingForOpponent, opponent, playerToMatches)
            ) {
                return rankOfPlayerSearchingForOpponent to rankOpponent
            }
        }

        if (rankedPlayers.size % 2 == 1 &&
            !ranksOfAlreadyAssignedPlayers.contains(rankedPlayers.size) &&
            !playedVsAnother(playerSearchingForOpponent, null, playerToMatches)
        ) {
            return rankOfPlayerSearchingForOpponent to rankedPlayers.size
        }

        return null
    }

    internal fun playedVsAnother(
        player1: PlayerEntity,
        player2: PlayerEntity?,
        playerToMatches: Map<PlayerEntity, Set<PlayedMatch>>
    ): Boolean {

        return playerToMatches.getValue(player1).stream()
            .anyMatch { match ->
                match.player1 == player1 && match.player2 == player2
                        || match.player1 == player2 && match.player2 == player1
            }
    }
}