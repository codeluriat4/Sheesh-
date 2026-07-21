package org.example.test.domain.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeTest {

    @Test
    fun `rejects negative values but allows zero`() {
        assertTrue(runCatching { Volume(-0.5) }.isFailure)
        assertTrue(Volume(0.0).isZero)
    }

    @Test
    fun `addition operates on the wrapped value`() {
        assertEquals(Volume(3.5), Volume(1.0) + Volume(2.5))
    }
}
