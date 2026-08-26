package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAnalysisResult
import com.example.ai.GeminiVisionService
import com.example.data.AppDatabase
import com.example.data.model.AuthProvider
import com.example.data.model.CompressionRecord
import com.example.data.model.FolderActivityLog
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ShareLinkRecord
import com.example.data.model.SharedFolder
import com.example.data.model.SharedFolderMember
import com.example.data.model.UserAccount
import com.example.data.repository.MediaRepository
import com.example.util.ArtisticStyle
import com.example.util.AuthManager
import com.example.util.CompressionEngine
import com.example.util.CompressionResult
import com.example.util.ImageCompressFormat
import com.example.util.ImageProcessingEngine
import com.example.util.SpiderFilterType
import com.example.util.StorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class SpiderSuitTheme(val displayName: String, val description: String) {
    CLASSIC("Classic Hero", "Red, Midnight Navy & Web Blue"),
    MILES_NEON("Spider-Verse Neon", "Vibrant Cyan, Crimson & Stealth Black"),
    IRON_SPIDER("Iron Spider Armor", "Stark Gold & Nanotech Crimson"),
    STEALTH_NOIR("Stealth Noir", "Cyber Monochrome & Dark Shadow")
}

enum class AiStudioEditMode(val title: String, val emoji: String) {
    AI_PROMPT_STUDIO("AI Public Wish", "💬"),
    ARTISTIC_STYLE("Artistic Styles", "🎨"),
    OBJECT_REMOVER("AI Eraser / Inpainting", "🧹"),
    SUPER_RESOLUTION("AI Upscale", "⚡"),
    SPIDER_VISION("Spider-Sense Vision", "🕸️")
}

data class MiracleUiState(
    val selectedCategory: MediaType? = null,
    val isFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val isGridView: Boolean = true,
    val selectedItemIds: Set<Long> = emptySet(),
    val currentSuitTheme: SpiderSuitTheme = SpiderSuitTheme.CLASSIC,
    val isSpiderSenseScanning: Boolean = false,
    val statusMessage: String? = null
)

data class CompressionUiState(
    val targetItem: MediaItem? = null,
    val targetFormat: ImageCompressFormat = ImageCompressFormat.WEBP_LOSSY,
    val quality: Int = 80,
    val scaleFactor: Float = 1.0f,
    val isCompressing: Boolean = false,
    val lastResult: CompressionResult? = null,
    val zipArchiveName: String = "spider_vault_archive"
)

data class AiStudioUiState(
    val targetItem: MediaItem? = null,
    val sourceBitmap: Bitmap? = null,
    val previewBitmap: Bitmap? = null,
    val activeEditMode: AiStudioEditMode = AiStudioEditMode.ARTISTIC_STYLE,
    val selectedArtStyle: ArtisticStyle = ArtisticStyle.SPIDER_VERSE,
    val styleIntensity: Float = 1.0f,
    val selectedFilter: SpiderFilterType = SpiderFilterType.ORIGINAL,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val rotation: Float = 0f,
    val flipHorizontal: Boolean = false,
    val upscaleFactor: Float = 2.0f,
    val isUpscaling: Boolean = false,
    val isInpainting: Boolean = false,
    val isAiAnalyzing: Boolean = false,
    val aiAnalysisResult: GeminiAnalysisResult? = null,
    val aiPrompt: String = "",
    val aiCustomResponse: String? = null,
    val isSaving: Boolean = false,
    val splitComparisonRatio: Float = 0.5f,
    val showSplitComparison: Boolean = false
)

class MiracleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MediaRepository
    val context: Context get() = getApplication<Application>().applicationContext

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MediaRepository(db.mediaDao(), db.compressionDao(), db.shareDao(), db.userDao())

        viewModelScope.launch {
            // 1. Seed initial users & restore session
            AuthManager.seedDefaultUsersIfEmpty(db.userDao())
            _currentUser.value = AuthManager.restoreSession(db.userDao(), application)

            // 2. Seed initial shared folders if empty
            val initialFolders = db.shareDao().getSharedFoldersSync()
            if (initialFolders.isEmpty()) {
                val folder1Id = db.shareDao().insertSharedFolder(
                    SharedFolder(
                        name = "Avengers Multiverse Vault",
                        description = "Confidential hero media and tech blueprint cache",
                        iconEmoji = "🛡️",
                        ownerAlias = "Peter Parker",
                        ownerEmail = "peter.parker@spider.net",
                        inviteCode = "AVENGERS-99"
                    )
                )
                db.shareDao().insertMember(
                    SharedFolderMember(
                        folderId = folder1Id,
                        alias = "Peter Parker",
                        email = "peter.parker@spider.net",
                        role = "OWNER",
                        avatarKey = "PP"
                    )
                )
                db.shareDao().insertMember(
                    SharedFolderMember(
                        folderId = folder1Id,
                        alias = "Miles Morales",
                        email = "miles.morales@spider.net",
                        role = "COLLABORATOR",
                        avatarKey = "MM"
                    )
                )
                db.shareDao().insertMember(
                    SharedFolderMember(
                        folderId = folder1Id,
                        alias = "Gwen Stacy",
                        email = "gwen.stacy@spider.net",
                        role = "COLLABORATOR",
                        avatarKey = "GS"
                    )
                )

                val folder2Id = db.shareDao().insertSharedFolder(
                    SharedFolder(
                        name = "NYC Web-Warriors Patrol",
                        description = "Live surveillance, hero photos, and patrol logs",
                        iconEmoji = "🕸️",
                        ownerAlias = "Miles Morales",
                        ownerEmail = "miles.morales@spider.net",
                        inviteCode = "SPIDER-NYC"
                    )
                )
                db.shareDao().insertMember(
                    SharedFolderMember(
                        folderId = folder2Id,
                        alias = "Miles Morales",
                        email = "miles.morales@spider.net",
                        role = "OWNER",
                        avatarKey = "MM"
                    )
                )
                db.shareDao().insertMember(
                    SharedFolderMember(
                        folderId = folder2Id,
                        alias = "Peter Parker",
                        email = "peter.parker@spider.net",
                        role = "COLLABORATOR",
                        avatarKey = "PP"
                    )
                )
            }

            // 3. Seed starter media if empty
            repository.totalMediaCount.collect { count ->
                if (count == 0) {
                    val seeds = StorageManager.createInitialSeedData(application)
                    if (seeds.isNotEmpty()) {
                        repository.insertMediaList(seeds)
                    }
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(MiracleUiState())
    val uiState: StateFlow<MiracleUiState> = _uiState.asStateFlow()

    private val _compressionState = MutableStateFlow(CompressionUiState())
    val compressionState: StateFlow<CompressionUiState> = _compressionState.asStateFlow()

    private val _aiStudioState = MutableStateFlow(AiStudioUiState())
    val aiStudioState: StateFlow<AiStudioUiState> = _aiStudioState.asStateFlow()

    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedFolders: StateFlow<List<SharedFolder>> = repository.allSharedFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shareLinks: StateFlow<List<ShareLinkRecord>> = repository.allShareLinks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSizeBytes: StateFlow<Long> = repository.totalSizeBytes
        .combine(MutableStateFlow(0L)) { size, _ -> size ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSavedBytes: StateFlow<Long> = repository.totalSpaceSavedBytes
        .combine(MutableStateFlow(0L)) { saved, _ -> saved ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val compressionHistory: StateFlow<List<CompressionRecord>> = repository.compressionHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMediaCount: StateFlow<Int> = repository.totalMediaCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered media based on UI category, favorites, search query
    val filteredMedia: StateFlow<List<MediaItem>> = combine(
        repository.allMedia,
        _uiState
    ) { items, state ->
        items.filter { item ->
            val matchesCategory = state.selectedCategory == null || item.type == state.selectedCategory
            val matchesFavorite = !state.isFavoritesOnly || item.isFavorite
            val matchesSearch = state.searchQuery.isBlank() ||
                    item.title.contains(state.searchQuery, ignoreCase = true) ||
                    item.tags.contains(state.searchQuery, ignoreCase = true)
            matchesCategory && matchesFavorite && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // AUTHENTICATION METHODS
    // ==========================================
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = AuthManager.authenticateUser(repository, email, password)
            if (user != null) {
                AuthManager.saveSession(context, user.sessionToken ?: "")
                _currentUser.value = user
                onSuccess()
            } else {
                onError("Invalid Spider-Network credentials. Please check email and password.")
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        alias: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                onError("An account with this email already exists.")
                return@launch
            }

            val newUser = AuthManager.registerUser(
                repository = repository,
                email = email,
                password = password,
                alias = alias,
                avatar = "🕸️",
                provider = AuthProvider.EMAIL_PASSWORD
            )
            AuthManager.saveSession(context, newUser.sessionToken ?: "")
            _currentUser.value = newUser
            onSuccess()
        }
    }

    fun loginWithSocial(
        provider: AuthProvider,
        email: String,
        alias: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = AuthManager.authenticateWithSocial(
                repository = repository,
                provider = provider,
                email = email,
                alias = alias,
                avatar = if (provider == AuthProvider.GOOGLE) "🌐" else "🍎"
            )
            AuthManager.saveSession(context, user.sessionToken ?: "")
            _currentUser.value = user
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.updateSessionToken(user.id, null)
            }
            AuthManager.clearSession(context)
            _currentUser.value = null
        }
    }

    fun toggleWebLock(enabled: Boolean, pin: String?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateWebLock(user.id, enabled, pin)
            _currentUser.value = user.copy(isWebLockEnabled = enabled, webLockPin = pin)
        }
    }

    // ==========================================
    // FILE SHARING & COLLABORATION
    // ==========================================
    fun createShareLink(link: ShareLinkRecord) {
        viewModelScope.launch {
            repository.createShareLink(link)
            _uiState.value = _uiState.value.copy(statusMessage = "Generated Spider-Link for ${link.targetTitle}")
        }
    }

    fun deleteShareLink(linkId: Long) {
        viewModelScope.launch {
            repository.deleteShareLink(linkId)
        }
    }

    fun createSharedFolder(name: String, description: String, iconEmoji: String = "🕸️") {
        viewModelScope.launch {
            val user = _currentUser.value
            val ownerAlias = user?.alias ?: "Peter Parker"
            val ownerEmail = user?.email ?: "peter.parker@spider.net"
            val invite = "SPIDER-${(1000..9999).random()}"

            val folder = SharedFolder(
                name = name,
                description = description,
                iconEmoji = iconEmoji,
                ownerAlias = ownerAlias,
                ownerEmail = ownerEmail,
                inviteCode = invite
            )
            val folderId = repository.createSharedFolder(folder)

            // Add owner as member
            repository.addFolderMember(
                SharedFolderMember(
                    folderId = folderId,
                    alias = ownerAlias,
                    email = ownerEmail,
                    role = "OWNER",
                    avatarKey = ownerAlias.take(2).uppercase()
                )
            )

            // Activity Log
            repository.logFolderActivity(
                FolderActivityLog(
                    folderId = folderId,
                    actorName = ownerAlias,
                    actionText = "Created shared vault '$name'"
                )
            )
        }
    }

    fun deleteSharedFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteSharedFolder(folderId)
        }
    }

    fun addFolderMember(member: SharedFolderMember) {
        viewModelScope.launch {
            repository.addFolderMember(member)
        }
    }

    fun logFolderActivity(folderId: Long, actorName: String, actionText: String) {
        viewModelScope.launch {
            repository.logFolderActivity(
                FolderActivityLog(
                    folderId = folderId,
                    actorName = actorName,
                    actionText = actionText
                )
            )
        }
    }

    fun assignMediaToFolder(mediaId: Long, folderId: Long?) {
        viewModelScope.launch {
            repository.assignMediaToFolder(mediaId, folderId)
        }
    }

    fun getMediaByFolder(folderId: Long): Flow<List<MediaItem>> = repository.getMediaByFolder(folderId)

    fun getFolderMembers(folderId: Long): Flow<List<SharedFolderMember>> = repository.getMembersForFolder(folderId)

    fun getFolderActivityLogs(folderId: Long): Flow<List<FolderActivityLog>> = repository.getActivityLogsForFolder(folderId)

    // ==========================================
    // UI ACTIONS
    // ==========================================
    fun setCategory(category: MediaType?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = if (_uiState.value.selectedCategory == category) null else category
        )
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(isFavoritesOnly = !_uiState.value.isFavoritesOnly)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleViewMode() {
        _uiState.value = _uiState.value.copy(isGridView = !_uiState.value.isGridView)
    }

    fun setSuitTheme(theme: SpiderSuitTheme) {
        _uiState.value = _uiState.value.copy(currentSuitTheme = theme)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    // Media item actions
    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            repository.updateMedia(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun deleteMedia(item: MediaItem) {
        viewModelScope.launch {
            StorageManager.deletePhysicalFile(item.filePath)
            repository.deleteMedia(item)
            _uiState.value = _uiState.value.copy(statusMessage = "Deleted ${item.title}")
        }
    }

    fun importUris(uris: List<Uri>) {
        viewModelScope.launch {
            var importedCount = 0
            for (uri in uris) {
                val mediaItem = StorageManager.importUri(context, uri)
                if (mediaItem != null) {
                    repository.insertMedia(mediaItem)
                    importedCount++
                }
            }
            _uiState.value = _uiState.value.copy(
                statusMessage = "Successfully secured $importedCount items to Miracle Vault!"
            )
        }
    }

    // Compression actions
    fun selectItemForCompression(item: MediaItem) {
        _compressionState.value = _compressionState.value.copy(
            targetItem = item,
            lastResult = null
        )
    }

    fun updateCompressionSettings(
        format: ImageCompressFormat = _compressionState.value.targetFormat,
        quality: Int = _compressionState.value.quality,
        scaleFactor: Float = _compressionState.value.scaleFactor
    ) {
        _compressionState.value = _compressionState.value.copy(
            targetFormat = format,
            quality = quality,
            scaleFactor = scaleFactor
        )
    }

    fun executeCompression() {
        val target = _compressionState.value.targetItem ?: return
        viewModelScope.launch {
            _compressionState.value = _compressionState.value.copy(isCompressing = true)
            val file = File(target.filePath)

            val result = if (target.type == MediaType.PHOTO) {
                CompressionEngine.compressImage(
                    context = context,
                    inputFile = file,
                    targetFormat = _compressionState.value.targetFormat,
                    quality = _compressionState.value.quality,
                    scaleFactor = _compressionState.value.scaleFactor
                )
            } else {
                CompressionEngine.compressFilesToZip(
                    context = context,
                    inputFiles = listOf(file),
                    archiveName = "${target.title.substringBeforeLast(".")}_archive"
                )
            }

            if (result.success && result.newMediaItem != null) {
                repository.insertMedia(result.newMediaItem)
                repository.recordCompression(
                    CompressionRecord(
                        fileName = target.title,
                        originalSizeBytes = result.originalSizeBytes,
                        compressedSizeBytes = result.compressedSizeBytes,
                        format = _compressionState.value.targetFormat.displayName
                    )
                )
            }

            _compressionState.value = _compressionState.value.copy(
                isCompressing = false,
                lastResult = result
            )
        }
    }

    fun compressSelectedItemsToZip(items: List<MediaItem>, archiveName: String) {
        viewModelScope.launch {
            _compressionState.value = _compressionState.value.copy(isCompressing = true)
            val files = items.map { File(it.filePath) }.filter { it.exists() }
            val result = CompressionEngine.compressFilesToZip(context, files, archiveName)

            if (result.success && result.newMediaItem != null) {
                repository.insertMedia(result.newMediaItem)
                repository.recordCompression(
                    CompressionRecord(
                        fileName = "$archiveName.zip (${items.size} files)",
                        originalSizeBytes = result.originalSizeBytes,
                        compressedSizeBytes = result.compressedSizeBytes,
                        format = "ZIP Archive"
                    )
                )
                _uiState.value = _uiState.value.copy(statusMessage = "Created ZIP Archive ($archiveName.zip)")
            }

            _compressionState.value = _compressionState.value.copy(
                isCompressing = false,
                lastResult = result
            )
        }
    }

    // ==========================================
    // AI STUDIO & ADVANCED EDITING ACTIONS
    // ==========================================
    fun loadItemForAiStudio(item: MediaItem) {
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(
                targetItem = item,
                selectedFilter = SpiderFilterType.ORIGINAL,
                selectedArtStyle = ArtisticStyle.SPIDER_VERSE,
                styleIntensity = 1.0f,
                brightness = 0f,
                contrast = 1f,
                saturation = 1f,
                rotation = 0f,
                flipHorizontal = false,
                showSplitComparison = false,
                aiAnalysisResult = null,
                aiCustomResponse = null
            )
            val bitmap = ImageProcessingEngine.loadBitmap(item.filePath)
            _aiStudioState.value = _aiStudioState.value.copy(
                sourceBitmap = bitmap,
                previewBitmap = bitmap
            )
            // Trigger automatic Spider-Sense scan
            if (bitmap != null) {
                triggerSpiderSenseAnalysis()
            }
        }
    }

    fun setAiEditMode(mode: AiStudioEditMode) {
        _aiStudioState.value = _aiStudioState.value.copy(activeEditMode = mode)
    }

    fun setSplitComparison(show: Boolean, ratio: Float = 0.5f) {
        _aiStudioState.value = _aiStudioState.value.copy(
            showSplitComparison = show,
            splitComparisonRatio = ratio
        )
    }

    fun applyArtisticStyle(style: ArtisticStyle, intensity: Float = _aiStudioState.value.styleIntensity) {
        _aiStudioState.value = _aiStudioState.value.copy(
            selectedArtStyle = style,
            styleIntensity = intensity
        )
        val source = _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            val stylized = ImageProcessingEngine.applyArtisticStyle(source, style, intensity)
            _aiStudioState.value = _aiStudioState.value.copy(previewBitmap = stylized)
        }
    }

    fun executeObjectRemoval(maskBitmap: Bitmap) {
        val currentBitmap = _aiStudioState.value.previewBitmap ?: _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isInpainting = true)
            val erased = ImageProcessingEngine.removeObjectWithMask(currentBitmap, maskBitmap)
            _aiStudioState.value = _aiStudioState.value.copy(
                isInpainting = false,
                previewBitmap = erased
            )
        }
    }

    fun executeSuperResolution(scaleFactor: Float = 2.0f, enhanceDetails: Boolean = true, noiseReduction: Boolean = true) {
        val currentBitmap = _aiStudioState.value.previewBitmap ?: _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isUpscaling = true, upscaleFactor = scaleFactor)
            val upscaled = ImageProcessingEngine.upscaleSuperResolution(
                source = currentBitmap,
                scaleFactor = scaleFactor,
                enhanceDetails = enhanceDetails,
                noiseReduction = noiseReduction
            )
            _aiStudioState.value = _aiStudioState.value.copy(
                isUpscaling = false,
                previewBitmap = upscaled
            )
        }
    }

    fun resetAiStudioEdits() {
        val original = _aiStudioState.value.sourceBitmap ?: return
        _aiStudioState.value = _aiStudioState.value.copy(
            previewBitmap = original,
            selectedFilter = SpiderFilterType.ORIGINAL,
            styleIntensity = 1.0f,
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            rotation = 0f,
            flipHorizontal = false,
            showSplitComparison = false
        )
    }

    fun updateFilter(filter: SpiderFilterType) {
        _aiStudioState.value = _aiStudioState.value.copy(selectedFilter = filter)
        recomputePreview()
    }

    fun updateAdjustments(
        brightness: Float = _aiStudioState.value.brightness,
        contrast: Float = _aiStudioState.value.contrast,
        saturation: Float = _aiStudioState.value.saturation
    ) {
        _aiStudioState.value = _aiStudioState.value.copy(
            brightness = brightness,
            contrast = contrast,
            saturation = saturation
        )
        recomputePreview()
    }

    fun rotateImage() {
        val newRotation = (_aiStudioState.value.rotation + 90f) % 360f
        _aiStudioState.value = _aiStudioState.value.copy(rotation = newRotation)
        recomputePreview()
    }

    fun flipImage() {
        _aiStudioState.value = _aiStudioState.value.copy(flipHorizontal = !_aiStudioState.value.flipHorizontal)
        recomputePreview()
    }

    private fun recomputePreview() {
        val source = _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            val processed = ImageProcessingEngine.applyAdjustmentsAndFilter(
                source = source,
                filter = _aiStudioState.value.selectedFilter,
                brightness = _aiStudioState.value.brightness,
                contrast = _aiStudioState.value.contrast,
                saturation = _aiStudioState.value.saturation,
                rotationAngle = _aiStudioState.value.rotation,
                flipHorizontal = _aiStudioState.value.flipHorizontal
            )
            _aiStudioState.value = _aiStudioState.value.copy(previewBitmap = processed)
        }
    }

    fun triggerSpiderSenseAnalysis(customInstruction: String? = null) {
        val bitmap = _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isAiAnalyzing = true)
            val result = GeminiVisionService.analyzeImageWithSpiderSense(bitmap, customInstruction)
            result.onSuccess { analysis ->
                _aiStudioState.value = _aiStudioState.value.copy(
                    isAiAnalyzing = false,
                    aiAnalysisResult = analysis
                )
            }.onFailure {
                _aiStudioState.value = _aiStudioState.value.copy(isAiAnalyzing = false)
            }
        }
    }

    fun askAiAssistant(prompt: String) {
        val bitmap = _aiStudioState.value.previewBitmap ?: _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isAiAnalyzing = true, aiPrompt = prompt)
            val response = GeminiVisionService.generateSuperheroAiEdit(bitmap, prompt)
            _aiStudioState.value = _aiStudioState.value.copy(
                isAiAnalyzing = false,
                aiCustomResponse = response.getOrNull()
            )
        }
    }

    fun applyAiPromptEdit(prompt: String) {
        if (prompt.isBlank()) return
        val source = _aiStudioState.value.sourceBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isAiAnalyzing = true, aiPrompt = prompt)

            val lower = prompt.lowercase()
            val newFilter: SpiderFilterType
            val newBrightness: Float
            val newContrast: Float
            val newSaturation: Float

            when {
                lower.contains("viral") || lower.contains("glow") || lower.contains("social") || lower.contains("trend") || lower.contains("pop") -> {
                    newFilter = SpiderFilterType.ELECTRIC_PULSE
                    newBrightness = 10f
                    newContrast = 1.3f
                    newSaturation = 1.45f
                }
                lower.contains("comic") || lower.contains("spider-verse") || lower.contains("cartoon") || lower.contains("hero") -> {
                    newFilter = SpiderFilterType.SPIDER_VERSE
                    newBrightness = 0f
                    newContrast = 1.4f
                    newSaturation = 1.5f
                }
                lower.contains("cinematic") || lower.contains("golden") || lower.contains("warm") || lower.contains("sunset") || lower.contains("movie") -> {
                    newFilter = SpiderFilterType.ORIGINAL
                    newBrightness = 14f
                    newContrast = 1.25f
                    newSaturation = 1.3f
                }
                lower.contains("cyberpunk") || lower.contains("neon") || lower.contains("future") || lower.contains("city") -> {
                    newFilter = SpiderFilterType.ELECTRIC_PULSE
                    newBrightness = 6f
                    newContrast = 1.35f
                    newSaturation = 1.6f
                }
                lower.contains("portrait") || lower.contains("studio") || lower.contains("face") || lower.contains("clean") || lower.contains("pro") -> {
                    newFilter = SpiderFilterType.ORIGINAL
                    newBrightness = 8f
                    newContrast = 1.18f
                    newSaturation = 1.15f
                }
                lower.contains("document") || lower.contains("scan") || lower.contains("text") || lower.contains("bw") || lower.contains("black and white") || lower.contains("mono") -> {
                    newFilter = SpiderFilterType.CYBER_NOIR
                    newBrightness = 18f
                    newContrast = 1.75f
                    newSaturation = 0.0f
                }
                else -> {
                    newFilter = SpiderFilterType.ORIGINAL
                    newBrightness = 10f
                    newContrast = 1.25f
                    newSaturation = 1.3f
                }
            }

            _aiStudioState.value = _aiStudioState.value.copy(
                selectedFilter = newFilter,
                brightness = newBrightness,
                contrast = newContrast,
                saturation = newSaturation
            )

            // Recompute preview bitmap
            val processed = ImageProcessingEngine.applyAdjustmentsAndFilter(
                source = source,
                filter = newFilter,
                brightness = newBrightness,
                contrast = newContrast,
                saturation = newSaturation,
                rotationAngle = _aiStudioState.value.rotation,
                flipHorizontal = _aiStudioState.value.flipHorizontal
            )

            // Query Gemini Vision for tailored guidance & story commentary
            val response = GeminiVisionService.generateSuperheroAiEdit(processed, prompt)

            _aiStudioState.value = _aiStudioState.value.copy(
                previewBitmap = processed,
                isAiAnalyzing = false,
                aiCustomResponse = response.getOrNull()
            )
            _uiState.value = _uiState.value.copy(statusMessage = "AI Public Wish Style Applied ✨")
        }
    }

    fun saveAiStudioResult(baseTitle: String) {
        val bitmap = _aiStudioState.value.previewBitmap ?: return
        viewModelScope.launch {
            _aiStudioState.value = _aiStudioState.value.copy(isSaving = true)
            val newItem = StorageManager.saveBitmapToVault(
                context = context,
                bitmap = bitmap,
                baseName = baseTitle.substringBeforeLast(".")
            )
            val analysis = _aiStudioState.value.aiAnalysisResult
            val finalItem = if (analysis != null) {
                newItem.copy(
                    aiAnalysis = analysis.description,
                    tags = (newItem.tags.split(",") + analysis.suggestedTags).distinct().joinToString(",")
                )
            } else newItem

            repository.insertMedia(finalItem)
            _aiStudioState.value = _aiStudioState.value.copy(isSaving = false)
            _uiState.value = _uiState.value.copy(statusMessage = "Spider-AI Edit Saved to Vault!")
        }
    }

    fun shareMedia(item: MediaItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                _uiState.value = _uiState.value.copy(statusMessage = "File not found on storage")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mime = if (item.mimeType.isNotBlank()) item.mimeType else "*/*"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(item.title, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share ${item.title} to...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(statusMessage = "Unable to share file: ${e.localizedMessage}")
        }
    }

    fun openMediaExternally(item: MediaItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) {
                _uiState.value = _uiState.value.copy(statusMessage = "File not found on storage")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val mime = if (item.mimeType.isNotBlank()) item.mimeType else "*/*"
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                clipData = ClipData.newRawUri(item.title, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(viewIntent, "Open ${item.title} with...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = _uiState.value.copy(statusMessage = "No compatible app found to open this file.")
        }
    }
}

