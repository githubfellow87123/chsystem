package com.tngtech.chsystem.service.rank

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class PlayerMatchesService {
    fun mapPlayersToMatches(
        players: Set<PlayerEntity>,
        alreadyPlayedMatches: Set<PlayedMatch>
    ): Map<PlayerEntity, Set<PlayedMatch>> {

        val playerToMatches = HashMap<PlayerEntity, Set<PlayedMatch>>()

        for (player in players) {
            val matchesOfPlayer = alreadyPlayedMatches.stream()
                .filter { match ->
                    match.player1 == player || match.player2 == player
                }
                .collect(Collectors.toSet())
            playerToMatches[player] = matchesOfPlayer
        }

        return playerToMatches
    }
}