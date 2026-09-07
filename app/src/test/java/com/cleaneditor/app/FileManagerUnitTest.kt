package com.cleaneditor.app

import com.cleaneditor.app.data.repository.FileManagerRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileManagerUnitTest {
    @Test fun acceptsValidNames() {
        assertTrue(FileManagerRepository.validateName("notas.txt").isSuccess)
        assertTrue(FileManagerRepository.validateName("meu arquivo.md").isSuccess)
    }

    @Test fun rejectsInvalidNames() {
        assertFalse(FileManagerRepository.validateName("").isSuccess)
        assertFalse(FileManagerRepository.validateName("../arquivo.txt").isSuccess)
        assertFalse(FileManagerRepository.validateName("pasta/arquivo.txt").isSuccess)
        assertFalse(FileManagerRepository.validateName("pasta" + "\\" + "arquivo.txt").isSuccess)
    }
}
