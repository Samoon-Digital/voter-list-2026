package com.samoondigital.yojnaplus.viewmodel

import org.junit.Assert.*
import org.junit.Test

class DownloadCompletionTransitionTest {
    @Test fun navigationWaitsForTheAdContinuationAndCanOnlyHappenOnce() {
        val transition = DownloadCompletionTransition()
        val batch = completedBatch()
        val id = requireNotNull(transition.begin(batch))
        assertFalse(transition.consumeNavigation(id, batch))
        transition.finishAd(id, batch)
        assertTrue(transition.consumeNavigation(id, batch))
        transition.finishAd(id, batch)
        assertFalse(transition.consumeNavigation(id, batch))
        assertNull(transition.begin(batch))
    }

    @Test fun oneFinishedFileDoesNotTriggerAnAdUntilTheWholeBatchIsSaved() {
        val transition = DownloadCompletionTransition()
        val batch = completedBatch().copy(
            isDownloading = true,
            downloadItems = listOf(item(1, DownloadStatus.Completed), item(2, DownloadStatus.Downloading)),
        )
        assertNull(transition.begin(batch))
        assertNull(transition.begin(batch.copy(isDownloading = false)))
        assertNotNull(transition.begin(completedBatch()))
    }

    @Test fun failedCancelledOrWaitingFilesDoNotCountAsASuccessfulBatch() {
        for (status in listOf(DownloadStatus.Failed, DownloadStatus.Cancelled, DownloadStatus.Waiting)) {
            val transition = DownloadCompletionTransition()
            assertNull(transition.begin(completedBatch().copy(
                downloadItems = listOf(item(1, DownloadStatus.Completed), item(2, status)),
            )))
        }
    }

    @Test fun emptyOrNonSuccessScreensCannotTriggerThePlacement() {
        val transition = DownloadCompletionTransition()
        assertNull(transition.begin(completedBatch().copy(downloadItems = emptyList())))
        assertNull(transition.begin(completedBatch().copy(step = ElectoralRollStep.Captcha)))
        assertNull(transition.begin(completedBatch().copy(isDownloading = true)))
    }

    @Test fun recreationAndRepeatedStateEmissionsKeepTheSamePendingOpportunity() {
        val transition = DownloadCompletionTransition()
        val batch = completedBatch()
        val id = requireNotNull(transition.begin(batch))
        repeat(3) { assertNull(transition.begin(batch)) }
        assertEquals(id, transition.state.value.id)
        assertTrue(transition.isActive(id, batch))
        transition.finishAd(id, batch)
        assertTrue(transition.consumeNavigation(id, batch))
    }

    @Test fun oldCallbacksCannotCompleteANewBatchAfterCancellation() {
        val transition = DownloadCompletionTransition()
        val batch = completedBatch()
        val old = requireNotNull(transition.begin(batch))
        transition.cancel()
        val next = requireNotNull(transition.begin(batch))
        assertTrue(next > old)
        transition.finishAd(old, batch)
        assertFalse(transition.state.value.ready)
        assertFalse(transition.consumeNavigation(old, batch))
        transition.finishAd(next, batch)
        assertTrue(transition.consumeNavigation(next, batch))
    }

    @Test fun leavingTheCompletedScreenInvalidatesAdAndNavigationCallbacks() {
        val transition = DownloadCompletionTransition()
        val batch = completedBatch()
        val id = requireNotNull(transition.begin(batch))
        val previousScreen = batch.copy(step = ElectoralRollStep.Parts)
        transition.finishAd(id, previousScreen)
        assertFalse(transition.isActive(id, previousScreen))
        assertFalse(transition.consumeNavigation(id, previousScreen))
        transition.cancel()
        assertFalse(transition.isActive(id, batch))
    }

    private fun completedBatch() = ElectoralRollUiState(
        step = ElectoralRollStep.Success,
        downloadItems = listOf(item(1, DownloadStatus.Completed), item(2, DownloadStatus.Completed)),
    )

    private fun item(number: Int, status: DownloadStatus) =
        ElectoralRollDownloadItem(partNumber = number, partName = "Part $number", status = status)
}
