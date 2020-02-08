package com.tngtech.chsystem.service

import com.tngtech.chsystem.entities.MatchEntity
import com.tngtech.chsystem.entities.PlayerEntity
import org.springframework.stereotype.Service

@Service
class MatchmakingService(
    private val rankingService: RankingService
) {

    fun generateMatchesForRound(
        roundIndex: Int,
        players: Set<PlayerEntity>,
        alreadyPlayedMatches: Set<MatchEntity>
    ): Set<MatchEntity> {
        return emptySet()
    }

}