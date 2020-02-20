package com.tngtech.chsystem.dto

class Pairings {

    private val pairings = ArrayList<Pair<Int, Int>>()

    fun addPairing(rankPlayer1: Int, rankPlayer2: Int): Boolean {

        for (pair in pairings) {
            if (pair.first == rankPlayer1 || pair.first == rankPlayer2 ||
                pair.second == rankPlayer1 || pair.second == rankPlayer2
            ) {
                return false
            }
        }

        pairings.add(rankPlayer1 to rankPlayer2)
        return true
    }

    fun removeLastPairing() {
        pairings.removeAt(pairings.size - 1)
    }

    fun getLastPairing(): Pair<Int, Int>? {
        if (pairings.isEmpty()) {
            return null
        }
        return pairings[pairings.size - 1]
    }

    fun getRanksOfAlreadyAssignedPlayers(): Set<Int> {
        val ranksOfAlreadyAssignedPlayers = HashSet<Int>()

        for (pairing in pairings) {
            ranksOfAlreadyAssignedPlayers.add(pairing.first)
            ranksOfAlreadyAssignedPlayers.add(pairing.second)
        }

        return ranksOfAlreadyAssignedPlayers
    }

    fun getNumberAlreadyAssignedPlayers(): Int {
        return pairings.size * 2
    }

    fun getListOfPairs(): List<Pair<Int, Int>> {
        return pairings
    }
}