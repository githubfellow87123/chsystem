package com.tngtech.chsystem.service

import com.tngtech.chsystem.entities.MatchEntity
import org.springframework.stereotype.Service

@Service
class MatchmakingService {

    // TODO implement logic, this is just a dummy right now
    fun generateMatchesForRound(roundIndex: Int, alreadyPlayedMatches: Set<MatchEntity>): Set<MatchEntity> {
        return emptySet()
    }
}