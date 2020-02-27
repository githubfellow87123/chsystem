package com.tngtech.chsystem.service.match

import com.tngtech.chsystem.dto.PlayedMatch
import com.tngtech.chsystem.entities.MatchEntity
import org.springframework.stereotype.Service

@Service
class MatchService {

    fun convertToPlayedMatches(matchEntities: Set<MatchEntity>): Set<PlayedMatch>? {

        val playedMatches = HashSet<PlayedMatch>()
        for (match in matchEntities) {
            val playedMatch = match.toPlayedMatch()
                ?: return null
            playedMatches.add(playedMatch)
        }

        return playedMatches
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