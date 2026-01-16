<template>
  <div class="book-detail-page">
    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>불러오는 중...</span>
    </div>

    <div v-else class="book-detail-content">
      <!-- Book Header Section -->
      <section class="book-header">
        <!-- Title with Edit -->
        <div v-if="!isEditingTitle" class="title-section">
          <h1 class="book-title">{{ book.title }}</h1>
          <div v-if="canEditBook" class="title-actions">
            <button @click="startEditTitle" class="icon-btn" title="제목 수정">✏️</button>
            <button @click="deleteBook" class="icon-btn danger" title="소설 삭제">🗑️</button>
          </div>
        </div>
        <div v-else class="title-edit-section">
          <input v-model="editTitleContent" class="form-control title-input" placeholder="제목을 입력하세요">
          <div class="title-edit-actions">
            <button @click="saveTitle" class="btn btn-primary btn-sm">저장</button>
            <button @click="cancelEditTitle" class="btn btn-outline btn-sm">취소</button>
          </div>
        </div>

        <!-- Book Meta Info -->
        <p class="book-meta">
          <span class="category-tag">{{ getCategoryName(book.categoryId) }}</span>
          <span class="divider">•</span>
          <span :class="['status-tag', book.status === 'COMPLETED' ? 'completed' : 'writing']">
            {{ book.status === 'COMPLETED' ? '완결' : '연재중' }}
          </span>
          <span class="divider">•</span>
          <span class="writer-count">👥 {{ book.sentences ? (new Set(book.sentences.map(s => s.writerId)).size) : 1 }}명 참여</span>
        </p>

        <!-- Vote Section -->
        <div class="vote-section">
          <button class="vote-card" :class="{ active: book.myVote === 'LIKE' }" @click="voteBook('LIKE')">
            <span class="vote-icon">👍</span>
            <span class="vote-label">개추</span>
            <span class="vote-count">{{ book.likeCount || 0 }}</span>
          </button>
          <button class="vote-card" :class="{ active: book.myVote === 'DISLIKE' }" @click="voteBook('DISLIKE')">
            <span class="vote-icon">👎</span>
            <span class="vote-label">비추</span>
            <span class="vote-count">{{ book.dislikeCount || 0 }}</span>
          </button>
        </div>

        <!-- Action Buttons -->
        <div class="action-section">
          <router-link v-if="book.status === 'COMPLETED'" :to="'/books/' + bookId + '/viewer'" class="btn btn-primary btn-action">
            📖 정주행 시작하기
          </router-link>
          <button v-if="book.status === 'WRITING' && isWriter" @click="completeBook" class="btn btn-complete">
            ✨ 완결 짓기
          </button>
        </div>
      </section>

      <!-- Main Content -->
      <main class="book-main">
        <!-- Sentence List -->
        <section class="sentence-section">
          <div v-for="sent in sortedSentences" :key="sent.sentenceId" class="sentence-card">
            <!-- Sentence Content -->
            <div class="sentence-body">
              <div v-if="editingSentenceId !== sent.sentenceId" class="sentence-text-wrap">
                <p class="sentence-text">{{ sent.content }}</p>
                <div v-if="canEditSentence(sent)" class="sentence-actions">
                  <button @click="startEditSentence(sent)" class="icon-btn small" title="수정">✏️</button>
                  <button @click="deleteSentence(sent)" class="icon-btn small danger" title="삭제">🗑️</button>
                </div>
              </div>
              <div v-else class="sentence-edit">
                <textarea v-model="editSentenceContent" class="form-control" rows="3"></textarea>
                <div class="sentence-edit-buttons">
                  <button @click="saveSentence(sent)" class="btn btn-primary btn-sm">저장</button>
                  <button @click="cancelEditSentence" class="btn btn-outline btn-sm">취소</button>
                </div>
              </div>
            </div>

            <!-- Sentence Footer -->
            <div class="sentence-footer">
              <span class="sentence-meta">
                <span class="seq-num">{{ sent.sequenceNo }}번째</span>
                <span class="writer-name">{{ sent.writerNicknm || '익명' }}</span>
              </span>
              <div class="sentence-votes">
                <button class="vote-mini" :class="{ active: sent.myVote === 'LIKE' }" @click="voteSentence(sent, 'LIKE')">
                  👍 {{ sent.likeCount || 0 }}
                </button>
                <button class="vote-mini" :class="{ active: sent.myVote === 'DISLIKE' }" @click="voteSentence(sent, 'DISLIKE')">
                  👎 {{ sent.dislikeCount || 0 }}
                </button>
              </div>
            </div>
          </div>
        </section>

        <!-- Writing Area -->
        <section v-if="book.status !== 'COMPLETED'" class="writing-section">
          <div class="writing-header">
            <h3>✍️ 다음 문장 이어쓰기</h3>
            <div v-show="activeTypers.length > 0" class="typing-indicator">
              <span class="typing-dots">
                <span></span><span></span><span></span>
              </span>
              <span>{{ activeTypers.join(', ') }}님이 입력 중...</span>
            </div>
          </div>

          <!-- Guest -->
          <div v-if="!authStore.isAuthenticated" class="guest-prompt">
            <p>이야기에 참여하려면 로그인이 필요합니다.</p>
            <button @click="authStore.openLogin" class="btn btn-primary">로그인하고 이어쓰기</button>
          </div>

          <!-- Logged In User -->
          <div v-else class="writing-form">
            <textarea 
              v-model="newSentence" 
              @input="handleInput" 
              @blur="handleBlur" 
              class="form-control writing-textarea"
              :placeholder="inputPlaceholder"
              :disabled="isInputDisabled"
            ></textarea>
            <div class="writing-footer">
              <span class="char-count">{{ newSentence.length }}/200</span>
              <button class="btn btn-primary" @click="submitSentence" :disabled="isInputDisabled || !newSentence.trim()">
                문장 등록
              </button>
            </div>
          </div>
        </section>

        <!-- Completed Badge -->
        <div v-else class="completed-badge-section">
          <span class="badge-completed">🎉 완결된 소설입니다</span>
        </div>

        <!-- Comments Section -->
        <section class="comments-section">
          <div class="comments-header">
            <h3>💬 감상평</h3>
            <div v-show="activeCommentTypers.length > 0" class="typing-indicator small">
              {{ activeCommentTypers.join(', ') }}님이 작성 중...
            </div>
          </div>

          <!-- Guest Prompt -->
          <div v-if="!authStore.isAuthenticated" class="guest-prompt small">
            <button @click="authStore.openLogin" class="btn btn-outline">로그인하고 감상평 남기기</button>
          </div>

          <!-- Comment List -->
          <div class="comment-list">
            <comment-node 
              v-for="comment in comments" 
              :key="comment.commentId" 
              :comment="comment"
              :current-user-id="authStore.user?.userId" 
              :user-role="authStore.user?.userRole" 
              @reply="submitReply"
              @edit="editComment" 
              @delete="deleteComment"
            />
          </div>

          <!-- Comment Form -->
          <div v-if="authStore.isAuthenticated" class="comment-form">
            <textarea 
              v-model="newComment" 
              @input="handleCommentInput" 
              @blur="handleCommentBlur" 
              class="form-control"
              placeholder="이 소설에 대한 감상평을 남겨주세요..."
            ></textarea>
            <div class="comment-form-footer">
              <button class="btn btn-primary" @click="submitComment" :disabled="!newComment.trim()">등록</button>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import CommentNode from '@/components/CommentNode.vue'
import { toast } from '@/utils/toast'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const bookId = route.params.id
const loading = ref(true)
const book = ref({})
const sentences = ref([])
const comments = ref([])
const links = ref({})

// Inputs
const newSentence = ref('')
const newComment = ref('')

// Editing
const isEditingTitle = ref(false)
const editTitleContent = ref('')
const editingSentenceId = ref(null)
const editSentenceContent = ref('')

// Real-time
const activeTypers = ref([])
const activeCommentTypers = ref([])
let stompClient = null
let typingTimeout = null
let commentTypingTimeout = null

// User focus tracking (for smart auto-scroll)
const isUserFocused = ref(false)

// Category Map
const categoryMap = { 'THRILLER': '스릴러', 'ROMANCE': '로맨스', 'FANTASY': '판타지', 'MYSTERY': '미스터리', 'SF': 'SF', 'DAILY': '일상' }
const getCategoryName = (code) => categoryMap[code] || code

// Computed
const sortedSentences = computed(() => {
  return sentences.value ? [...sentences.value].sort((a, b) => a.sequenceNo - b.sequenceNo) : []
})

const isWriter = computed(() => {
  return authStore.user && book.value.writerId && (book.value.writerId === authStore.user.userId)
})

const isAdmin = computed(() => {
  return authStore.user && (authStore.user.userRole === 'ADMIN' || authStore.user.userRole === 'ROLE_ADMIN')
})

const canEditBook = computed(() => isWriter.value || isAdmin.value)

const isInputDisabled = computed(() => {
  if (!authStore.isAuthenticated) return true
  if (book.value.status === 'COMPLETED') return true
  if (activeTypers.value.length > 0) return true
  if (authStore.user && book.value.lastWriterUserId === authStore.user.userId) return true
  return false
})

const inputPlaceholder = computed(() => {
  if (book.value.status === 'COMPLETED') return "소설이 완결되었습니다."
  if (authStore.user && book.value.lastWriterUserId === authStore.user.userId) return "연속으로 작성할 수 없습니다. 다른 분이 이어서 써주시기를 기다려주세요! ⏳"
  if (activeTypers.value.length > 0) { const typer = activeTypers.value[0]; return `${typer}님이 작성 중입니다... ✍️` }
  return "당신의 상상력을 펼쳐보세요... (최대 200자)"
})

// Methods
onMounted(async () => {
  await authStore.fetchUserProfile()
  fetchBookDetail()
  fetchComments()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) stompClient.deactivate()
})

const fetchBookDetail = async () => {
  try {
    const res = await axios.get(`/books/${bookId}/view`)
    book.value = res.data.data
    links.value = book.value._links || {}
    sentences.value = book.value.sentences || []
  } catch (e) {
    if (e.response && (e.response.status === 401 || e.response.status === 403)) {
      toast.warning('로그인이 필요합니다.')
      authStore.openLogin()
    } else {
      toast.error('소설 정보를 불러올 수 없습니다.')
    }
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  const url = links.value.comments ? links.value.comments.href : `/reactions/comments/${bookId}`
  try {
    const res = await axios.get(url)
    comments.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

// WebSocket
const connectWebSocket = () => {
  stompClient = new Client({
    brokerURL: 'ws://localhost:8082/ws',
    webSocketFactory: () => new SockJS('/ws'), 
    debug: () => {},
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })

  stompClient.onConnect = (frame) => {
    stompClient.subscribe(`/topic/typing/${bookId}`, (message) => handleTypingStatus(JSON.parse(message.body)))
    stompClient.subscribe(`/topic/comment-typing/${bookId}`, (message) => handleCommentTypingStatus(JSON.parse(message.body)))
    stompClient.subscribe(`/topic/sentences/${bookId}`, (message) => handleNewSentence(JSON.parse(message.body)))
    stompClient.subscribe(`/topic/books/${bookId}/votes`, (message) => handleVoteUpdate(JSON.parse(message.body)))
    stompClient.subscribe(`/topic/comments/${bookId}`, (message) => handleNewComment(JSON.parse(message.body)))
    stompClient.subscribe(`/topic/books/${bookId}/status`, (message) => handleBookStatusUpdate(JSON.parse(message.body)))
  }

  stompClient.onStompError = (frame) => {
    console.error('[STOMP] Error:', frame.headers['message'])
  }

  stompClient.activate()
}

// Handlers
const handleTypingStatus = (data) => {
  const nick = data.userNickname || data.nickname
  if (data.isTyping) {
     if (nick && !activeTypers.value.includes(nick)) activeTypers.value.push(nick)
  } else {
     activeTypers.value = activeTypers.value.filter(n => n !== nick)
  }
}

const handleCommentTypingStatus = (data) => {
  const nick = data.userNickname || data.nickname
  if (data.isTyping) {
     if (nick && !activeCommentTypers.value.includes(nick)) activeCommentTypers.value.push(nick)
  } else {
     activeCommentTypers.value = activeCommentTypers.value.filter(n => n !== nick)
  }
}

const handleNewSentence = (event) => {
  sentences.value.push({
    sentenceId: event.sentenceId || Date.now(), 
    content: event.content, 
    sequenceNo: event.sequenceNo,
    writerNicknm: event.writerNickname, 
    writerId: event.writerId, 
    likeCount: 0, 
    dislikeCount: 0
  })
  if (book.value) book.value.lastWriterUserId = event.writerId
  
  // 사용자가 입력 중이 아닐 때만 자동 스크롤
  if (!isUserFocused.value) {
    nextTick(() => { 
      const sentenceList = document.querySelector('.sentence-section')
      if (sentenceList) {
        const lastCard = sentenceList.querySelector('.sentence-card:last-child')
        if (lastCard) {
          lastCard.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }
      }
    })
  }
}

const handleNewComment = (comment) => {
  if (comment.nickname && !comment.writerNicknm) {
    comment.writerNicknm = comment.nickname
  }
  comments.value.push(comment)
}

const handleBookStatusUpdate = (update) => {
  // 완결 상태 실시간 업데이트
  if (update.bookId === parseInt(bookId)) {
    book.value.status = update.status
    if (update.status === 'COMPLETED') {
      toast.success('🎉 소설이 완결되었습니다!')
    }
  }
}

const handleVoteUpdate = (update) => {
  if (update.targetType === 'BOOK' && update.targetId === parseInt(bookId)) {
    book.value.likeCount = update.likeCount; book.value.dislikeCount = update.dislikeCount
  } else if (update.targetType === 'SENTENCE') {
    const sentence = sentences.value.find(s => s.sentenceId === update.targetId)
    if (sentence) { sentence.likeCount = update.likeCount; sentence.dislikeCount = update.dislikeCount }
  }
}

// Typing Emitter
const handleInput = () => {
    isUserFocused.value = true
    if (typingTimeout) clearTimeout(typingTimeout)
    sendTyping(true)
    typingTimeout = setTimeout(() => sendTyping(false), 2000)
}
const handleBlur = () => { 
    isUserFocused.value = false
    sendTyping(false) 
}

const sendTyping = (status) => {
    if (!stompClient || !stompClient.connected) return
    stompClient.publish({
        destination: `/app/typing/${bookId}`,
        body: JSON.stringify({ userNickname: authStore.user?.userNicknm, isTyping: status })
    })
}

const handleCommentInput = () => {
    isUserFocused.value = true
    if (commentTypingTimeout) clearTimeout(commentTypingTimeout)
    sendCommentTyping(true)
    commentTypingTimeout = setTimeout(() => sendCommentTyping(false), 2000)
}
const handleCommentBlur = () => { 
    isUserFocused.value = false
    sendCommentTyping(false) 
}

const sendCommentTyping = (status) => {
    if (!stompClient || !stompClient.connected) return
    stompClient.publish({
        destination: `/app/comment-typing/${bookId}`,
        body: JSON.stringify({ userNickname: authStore.user?.userNicknm, isTyping: status })
    })
}

// Actions
const submitSentence = async () => {
    if (!newSentence.value.trim()) return
    const url = links.value['append-sentence'] ? links.value['append-sentence'].href : `/books/${bookId}/sentences`
    try {
        await axios.post(url, { content: newSentence.value })
        toast.success('문장이 등록되었습니다!')
        newSentence.value = ''
        await fetchBookDetail()
    } catch(e) { 
      console.error(e)
      toast.error(e.response?.data?.message || '문장 등록에 실패했습니다.')
    }
}

const submitComment = async () => {
    if (!newComment.value.trim()) return
    postComment(newComment.value, null, () => { newComment.value = '' })
}

const submitReply = (payload) => {
    postComment(payload.content, payload.parentId, payload.callback, payload.link)
}

const postComment = async (content, parentId, callback, link) => {
    if (!authStore.isAuthenticated) { authStore.openLogin(); return }
    const url = link || '/reactions/comments'
    try {
        await axios.post(url, { bookId: bookId, content, parentId })
        if (callback) callback()
        fetchComments()
    } catch (e) { toast.error('등록 실패') }
}

const editComment = async (payload) => {
    try {
        await axios.patch(`/reactions/comments/${payload.commentId}`, { content: payload.content })
        if (payload.callback) payload.callback()
        fetchComments()
    } catch(e) { toast.error('수정 실패') }
}

const deleteComment = async (payload) => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
        await axios.delete(`/reactions/comments/${payload.commentId}`)
        fetchComments()
    } catch(e) { toast.error('삭제 실패') }
}

const voteBook = async (voteType) => {
    if (!authStore.isAuthenticated) { authStore.openLogin(); return }
    try {
        const url = links.value['vote-book'] ? links.value['vote-book'].href : '/reactions/votes/books'
        await axios.post(url, { bookId: parseInt(bookId), voteType })
        await fetchBookDetail()
    } catch(e) {
        toast.error(e.response?.data?.message || '투표 처리 중 오류가 발생했습니다.')
    }
}

const voteSentence = async (sent, voteType) => {
    if (!authStore.isAuthenticated) { authStore.openLogin(); return }
    try {
        const url = `/reactions/votes/sentences/${sent.sentenceId}`
        await axios.post(url, { voteType })
        await fetchBookDetail()
    } catch(e) {
        toast.error(e.response?.data?.message || '투표 처리 중 오류가 발생했습니다.')
    }
}

const completeBook = async () => {
    if (!confirm('완결하시겠습니까?')) return
    try {
        await axios.post(`/books/${bookId}/complete`)
        toast.success('완결되었습니다!')
        await fetchBookDetail()
    } catch(e) { toast.error('완결 처리 실패') }
}

const deleteBook = async () => {
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
        await axios.delete(`/books/${bookId}`)
        router.push('/')
    } catch(e) { toast.error('삭제 실패') }
}

// Sentence Edit
const canEditSentence = (sent) => (authStore.user && sent.writerId === authStore.user.userId) || isAdmin.value

const startEditSentence = (sent) => {
    const last = sortedSentences.value[sortedSentences.value.length - 1]
    if (sent.sentenceId !== last.sentenceId) { toast.warning('마지막 문장만 수정 가능'); return }
    editSentenceContent.value = sent.content
    editingSentenceId.value = sent.sentenceId
    sendTyping(true)
}

const cancelEditSentence = () => {
    editingSentenceId.value = null
    sendTyping(false)
}

const saveSentence = async (sent) => {
    try {
        await axios.patch(`/books/${bookId}/sentences/${sent.sentenceId}`, { content: editSentenceContent.value })
        sent.content = editSentenceContent.value
        editingSentenceId.value = null
        sendTyping(false)
    } catch(e) { toast.error('수정 실패') }
}

const deleteSentence = async (sent) => {
    const last = sortedSentences.value[sortedSentences.value.length - 1]
    if (sent.sentenceId !== last.sentenceId) { toast.warning('마지막 문장만 삭제 가능'); return }
    if (!confirm('삭제하시겠습니까?')) return
    try {
        await axios.delete(`/books/${bookId}/sentences/${sent.sentenceId}`)
        sentences.value = sentences.value.filter(s => s.sentenceId !== sent.sentenceId)
    } catch(e) { toast.error('삭제 실패') }
}

// Title Edit
const startEditTitle = () => { editTitleContent.value = book.value.title; isEditingTitle.value = true }
const cancelEditTitle = () => { isEditingTitle.value = false }
const saveTitle = async () => {
    try {
        await axios.patch(`/books/${bookId}`, { title: editTitleContent.value })
        book.value.title = editTitleContent.value
        isEditingTitle.value = false
    } catch(e) { toast.error('제목 수정 실패') }
}
</script>

<style scoped>
/* Page Layout */
.book-detail-page {
  max-width: 100%;
  min-height: 80vh;
}

.book-detail-content {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 15px;
  color: var(--text-muted);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Book Header */
.book-header {
  text-align: center;
  padding: 0 20px 30px;
  margin-bottom: 30px;
}

.title-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.book-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-color);
  margin: 0;
  line-height: 1.3;
}

.title-actions {
  display: flex;
  gap: 5px;
}

.icon-btn {
  background: transparent;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 8px;
  transition: all 0.2s;
  opacity: 0.6;
}

.icon-btn:hover {
  opacity: 1;
  background: rgba(0,0,0,0.05);
}

.icon-btn.danger:hover {
  background: rgba(255, 100, 100, 0.1);
}

.icon-btn.small {
  font-size: 0.9rem;
  padding: 3px 6px;
}

.title-edit-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.title-input {
  font-size: 1.5rem;
  text-align: center;
  max-width: 400px;
}

.title-edit-actions {
  display: flex;
  gap: 8px;
}

/* Book Meta */
.book-meta {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: var(--text-muted);
  font-size: 0.95rem;
  margin-bottom: 25px;
}

.divider {
  color: var(--border-color);
}

.category-tag {
  background: linear-gradient(135deg, var(--primary-color), #FF6B9D);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-tag.writing {
  background: #E8F5E9;
  color: #2E7D32;
}

.status-tag.completed {
  background: #E3F2FD;
  color: #1565C0;
}

.writer-count {
  font-weight: 600;
}

/* Vote Section */
.vote-section {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 25px;
}

.vote-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 85px;
  height: 85px;
  background: white;
  border: 2px solid #eee;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

.vote-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  border-color: var(--border-color);
}

.vote-card.active {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, #FFF5F8, #FFE8EF);
}

.vote-icon {
  font-size: 1.8rem;
  margin-bottom: 2px;
}

.vote-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-weight: 600;
}

.vote-count {
  font-size: 1rem;
  font-weight: 800;
  color: var(--text-color);
}

.vote-card.active .vote-count {
  color: var(--primary-color);
}

/* Action Section */
.action-section {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.btn-action {
  padding: 14px 35px;
  font-size: 1.1rem;
  font-weight: 700;
  border-radius: 30px;
}

.btn-complete {
  padding: 12px 25px;
  background: transparent;
  border: 2px solid #ff6b6b;
  color: #ff6b6b;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-complete:hover {
  background: #ff6b6b;
  color: white;
}

/* Main Content */
.book-main {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Sentence Section */
.sentence-section {
  margin-bottom: 30px;
}

.sentence-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 15px;
  border-left: 4px solid var(--primary-color);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  transition: all 0.2s ease;
}

.sentence-card:hover {
  box-shadow: 0 8px 25px rgba(232, 93, 117, 0.12);
}

.sentence-body {
  margin-bottom: 15px;
}

.sentence-text-wrap {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.sentence-text {
  flex: 1;
  font-size: 1.1rem;
  line-height: 1.9;
  color: var(--text-color);
  margin: 0;
}

.sentence-actions {
  display: flex;
  gap: 3px;
  opacity: 0;
  transition: opacity 0.2s;
}

.sentence-card:hover .sentence-actions {
  opacity: 1;
}

.sentence-edit {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sentence-edit-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sentence-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.sentence-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

.seq-num {
  color: var(--primary-color);
  font-weight: 700;
}

.writer-name {
  color: var(--text-muted);
  font-weight: 500;
}

.sentence-votes {
  display: flex;
  gap: 8px;
}

.vote-mini {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  background: white;
  border: 2px solid #eee;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.vote-mini:hover {
  border-color: var(--border-color);
  background: #FFF5F8;
}

.vote-mini.active {
  border-color: var(--primary-color);
  background: #FFF0F5;
  color: var(--primary-color);
}

/* Writing Section */
.writing-section {
  background: white;
  border-radius: 25px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 8px 30px rgba(232, 93, 117, 0.1);
}

.writing-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.writing-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
  color: var(--primary-color);
  font-weight: 600;
}

.typing-indicator.small {
  font-size: 0.8rem;
}

.typing-dots {
  display: flex;
  gap: 3px;
}

.typing-dots span {
  width: 5px;
  height: 5px;
  background: var(--primary-color);
  border-radius: 50%;
  animation: typingBounce 1s infinite;
}

.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.guest-prompt {
  text-align: center;
  padding: 30px 15px;
  background: rgba(0,0,0,0.02);
  border-radius: 15px;
  border: 1px dashed rgba(0,0,0,0.1);
}

.guest-prompt p {
  color: var(--text-muted);
  margin-bottom: 15px;
  font-size: 0.95rem;
}

.guest-prompt.small {
  padding: 20px;
  margin-bottom: 20px;
}

.writing-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.writing-textarea {
  min-height: 130px;
  border-radius: 15px;
  font-size: 1rem;
  resize: none;
  padding: 18px;
}

.writing-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 0.85rem;
  color: var(--text-muted);
}

/* Completed Badge */
.completed-badge-section {
  text-align: center;
  padding: 25px;
  margin-bottom: 30px;
}

.badge-completed {
  display: inline-block;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  color: #1565C0;
  padding: 12px 25px;
  border-radius: 30px;
  font-size: 1rem;
  font-weight: 700;
}

/* Comments Section */
.comments-section {
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%);
  border: 2px solid #BAE6FD;
  border-radius: 25px;
  padding: 30px;
  margin-bottom: 40px;
}

.comments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.comments-header h3 {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 700;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 25px;
}

.comment-form textarea {
  min-height: 100px;
  border-radius: 15px;
  resize: none;
  padding: 15px;
}

.comment-form-footer {
  display: flex;
  justify-content: flex-end;
}

.comment-list {
  margin-top: 20px;
}

/* Responsive */
@media (max-width: 768px) {
  .book-title {
    font-size: 1.6rem;
  }
  
  .vote-section {
    gap: 15px;
  }
  
  .vote-card {
    width: 75px;
    height: 75px;
  }
  
  .vote-icon {
    font-size: 1.5rem;
  }
  
  .sentence-card {
    padding: 20px;
  }
  
  .sentence-text {
    font-size: 1rem;
  }
  
  .sentence-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .sentence-votes {
    width: 100%;
    justify-content: flex-end;
  }
  
  .writing-section,
  .comments-section {
    padding: 22px;
    border-radius: 20px;
  }
  
  .action-section {
    flex-direction: column;
    width: 100%;
    padding: 0 20px;
  }
  
  .btn-action,
  .btn-complete {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .vote-card {
    width: 65px;
    height: 65px;
    border-radius: 16px;
  }
  
  .vote-icon {
    font-size: 1.3rem;
  }
  
  .vote-label {
    font-size: 0.7rem;
  }
  
  .vote-count {
    font-size: 0.9rem;
  }
  
  .sentence-card {
    padding: 16px;
    border-radius: 15px;
  }
  
  .vote-mini {
    padding: 5px 10px;
    font-size: 0.8rem;
  }
}
</style>
