package com.tngtech.chsystem.controller

import com.tngtech.chsystem.dao.TournamentRepository
import com.tngtech.chsystem.dto.RankingScore
import com.tngtech.chsystem.dto.Score
import com.tngtech.chsystem.dto.StatisticScore
import com.tngtech.chsystem.entities.PlayerEntity
import com.tngtech.chsystem.entities.TournamentEntity
import com.tngtech.chsystem.entities.TournamentState
import com.tngtech.chsystem.model.StandingsModel
import com.tngtech.chsystem.model.TournamentModel
import com.tngtech.chsystem.service.match.MatchService
import com.tngtech.chsystem.service.matchmaking.MatchmakingCode
import com.tngtech.chsystem.service.matchmaking.MatchmakingService
import com.tngtech.chsystem.service.rank.RankingService
import com.tngtech.chsystem.service.score.ScoreService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class TournamentControllerUnitTest {

    @MockK
    lateinit var tournamentRepository: TournamentRepository

    @MockK
    lateinit var matchmakingService: MatchmakingService

    @MockK
    lateinit var matchService: MatchService

    @MockK
    lateinit var rankingService: RankingService

    @MockK
    lateinit var scoreService: ScoreService

    @InjectMockKs
    lateinit var tournamentController: TournamentController

    private val player1 = PlayerEntity(name = "Alex")
    private val player2 = PlayerEntity(name = "Bert")

    private val rankedPlayers = listOf(player1, player2)

    @Test
    fun insertPlayer() {
        val tournamentEntitySlot = slot<TournamentEntity>()

        every {
            tournamentRepository.save(capture(tournamentEntitySlot))
        } answers {
            tournamentEntitySlot.captured
        }

        tournamentController.insertTournament(TournamentModel())

        val tournamentEntity = tournamentEntitySlot.captured
        verify { tournamentRepository.save(tournamentEntity) }
        assertThat(tournamentEntity.id).isNotNull()
        assertThat(tournamentEntity.date).isToday()
        assertThat(tournamentEntity.roundIndex).isEqualTo(0)
        assertThat(tournamentEntity.state).isEqualTo(TournamentState.INITIALIZING)
    }

    @Test
    fun `date is settable with insertPlayer`() {
        val tournamentEntitySlot = slot<TournamentEntity>()
        val date = LocalDate.of(2000, 12, 27)

        every {
            tournamentRepository.save(capture(tournamentEntitySlot))
        } answers {
            tournamentEntitySlot.captured
        }

        tournamentController.insertTournament(TournamentModel(date = date))

        val tournamentEntity = tournamentEntitySlot.captured
        verify { tournamentRepository.save(tournamentEntity) }
        assertThat(tournamentEntity.id).isNotNull()
        assertThat(tournamentEntity.date).isEqualTo(date)
        assertThat(tournamentEntity.roundIndex).isEqualTo(0)
        assertThat(tournamentEntity.state).isEqualTo(TournamentState.INITIALIZING)
    }

    @Test
    fun startTournament() {
        val player1 = PlayerEntity(name = "Alex")
        val player2 = PlayerEntity(name = "Bert")
        val tournamentEntity = TournamentEntity()
        tournamentEntity.addPlayer(player1)
        tournamentEntity.addPlayer(player2)

        val tournamentEntitySlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { tournamentRepository.save(capture(tournamentEntitySlot)) } answers { tournamentEntitySlot.captured }

        val startedTournament = tournamentController.startTournament(tournamentEntity.id)

        assertThat(startedTournament.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(startedTournament.roundIndex).isEqualTo(0)
        assertThat(tournamentEntitySlot.captured.state).isEqualTo(TournamentState.IN_PROGRESS)
        assertThat(tournamentEntitySlot.captured.roundIndex).isEqualTo(0)
    }

    @Test
    fun `startTournament throws exception if tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.startTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `startTournament throws exception if tournament is already finished`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.startTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun nextRoundOfTournament() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS, roundIndex = 1)
        val tournamentEntitySlot = slot<TournamentEntity>()

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.SUCCESSFUL
        every { tournamentRepository.save(capture(tournamentEntitySlot)) } answers { tournamentEntitySlot.captured }

        val tournamentModel = tournamentController.nextRoundOfTournament(tournamentEntity.id)

        assertThat(tournamentModel.roundIndex).isEqualTo(2)
        assertThat(tournamentEntitySlot.captured.roundIndex).isEqualTo(2)
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if matches can not be generated`() {
        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.NO_VALID_MATCHES_FOR_NEXT_ROUND_AVAILABLE

        assertFailsWith<TournamentController.UnableToGenerateMatchesException> {
            tournamentController.nextRoundOfTournament(tournamentEntity.id)
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if results of current round were not entered`() {
        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)

        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchmakingService.generateMatchesForNextRound(tournamentEntity) } returns MatchmakingCode.MISSING_RESULTS_OF_CURRENT_ROUND

        assertFailsWith<TournamentController.UnableToGenerateMatchesException> {
            tournamentController.nextRoundOfTournament(tournamentEntity.id)
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament is already finished`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun `nextRoundOfTournament throws exception if tournament has not started`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.INITIALIZING)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.nextRoundOfTournament(
                tournamentEntity.id
            )
        }
    }

    @Test
    fun finishTournament() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        tournamentEntity.addPlayer(player1)
        tournamentEntity.addPlayer(player2)
        val savedTournamentSlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false
        every { tournamentRepository.save(capture(savedTournamentSlot)) } answers {
            savedTournamentSlot.captured
        }
        every { rankingService.rankPlayers(tournamentEntity) } returns rankedPlayers

        val finishedTournament = tournamentController.finishTournament(tournamentEntity.id)

        verify { tournamentRepository.save(savedTournamentSlot.captured) }
        assertThat(savedTournamentSlot.captured.state).isEqualTo(TournamentState.DONE)
        assertThat(savedTournamentSlot.captured.getRankOfPlayer(player1)).isEqualTo(1)
        assertThat(savedTournamentSlot.captured.getRankOfPlayer(player2)).isEqualTo(2)
        assertThat(finishedTournament.state).isEqualTo(TournamentState.DONE)
    }

    @Test
    fun `finishTournament throws exception when not all matches have results`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns true

        assertFailsWith<TournamentController.UnableToFinishTournamentException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament throws exception when tournament doesn't exist`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.IN_PROGRESS)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament throws exception when tournament is in wrong state`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.INITIALIZING)
        val savedTournamentSlot = slot<TournamentEntity>()
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false
        every { tournamentRepository.save(capture(savedTournamentSlot)) } answers {
            savedTournamentSlot.captured
        }

        assertFailsWith<TournamentController.TournamentInWrongStateException> {
            tournamentController.finishTournament(
                tournamentEntity.id
            )
        }
        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
    }

    @Test
    fun `finishTournament tournament can be finished twice`() {

        val tournamentEntity = TournamentEntity(state = TournamentState.DONE)
        every { tournamentRepository.findByIdOrNull(tournamentEntity.id) } returns tournamentEntity
        every { matchService.isResultMissing(tournamentEntity.matches) } returns false

        val finishedTournament = tournamentController.finishTournament(tournamentEntity.id)

        verify(exactly = 0) { tournamentRepository.save(any<TournamentEntity>()) }
        assertThat(finishedTournament.state).isEqualTo(TournamentState.DONE)
    }

    @Test
    fun getStandings() {

        val tournament = TournamentEntity(state = TournamentState.DONE)
        val score1 = Score(
            RankingScore(3, 0.0, 0.66, 0.33),
            StatisticScore(1, 0, 0, 2, 1)
        )
        val score2 = Score(
            RankingScore(0, 3.0, 0.33, 0.66),
            StatisticScore(0, 1, 0, 1, 2)
        )

        every { tournamentRepository.findByIdOrNull(tournament.id) } returns tournament
        every { scoreService.calculateScores(tournament) } returns mapOf(player1 to score1, player2 to score2)

        val standings = tournamentController.getStandings(tournament.id)

        assertThat(standings).hasSize(2)
        assertThat(standings).contains(
            StandingsModel(
                playerName = "Alex",
                score = 3,
                matchWins = 1,
                matchLosses = 0,
                matchDraws = 0,
                opponentAverageScore = 0.0,
                gameWins = 2,
                gameLosses = 1,
                gameWinPercentage = 0.66,
                opponentAverageGameWinPercentage = 0.33
            )
        )
        assertThat(standings).contains(
            StandingsModel(
                playerName = "Bert",
                score = 0,
                matchWins = 0,
                matchLosses = 1,
                matchDraws = 0,
                opponentAverageScore = 3.0,
                gameWins = 1,
                gameLosses = 2,
                gameWinPercentage = 0.33,
                opponentAverageGameWinPercentage = 0.66
            )
        )
    }

    @Test
    fun `getStandings throws exception when tournament doesn't exist`() {

        val tournament = TournamentEntity()
        every { tournamentRepository.findByIdOrNull(tournament.id) } returns null

        assertFailsWith<TournamentController.TournamentDoesNotExistException> {
            tournamentController.getStandings(tournament.id)
        }
    }
}