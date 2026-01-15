<template>
  <div>
    <!-- Loading State -->
    <div v-if="loading" class="text-center" style="padding: 50px;">
      <h2>Loading...</h2>
    </div>

    <div v-else>
      <!-- Title Section with Edit -->
      <div id="book-header" class="text-center mb-4 fade-in">
        <div v-if="!isEditingTitle" style="display: flex; justify-content: center; align-items: center; gap: 10px;">
          <h1 style="margin-bottom: 10px;">{{ book.title }}</h1>
          <button v-if="canEditBook" @click="startEditTitle" class="btn btn-ghost btn-sm" title="제목 수정">✏️</button>
          <button v-if="canEditBook" @click="deleteBook" class="btn btn-ghost btn-sm" title="소설 삭제"
            style="color: #ff6b6b;">🗑️</button>
        </div>
        <div v-else
          style="display: flex; justify-content: center; align-items: center; gap: 10px; margin-bottom: 10px;">
          <input v-model="editTitleContent" class="form-control"
            style="font-size: 1.5rem; width: auto; text-align: center;">
          <button @click="saveTitle" class="btn btn-primary btn-sm">저장</button>
          <button @click="cancelEditTitle" class="btn btn-outline btn-sm">취소</button>
        </div>

        <p style="color: var(--text-muted);">
          <span>{{ getCategoryName(book.categoryId) }}</span> |
          <span>{{ book.status }}</span> |
          작가 <span>{{ book.sentences ? (new Set(book.sentences.map(s => s.writerId)).size) : 1 }}</span>명
        </p>
        <div class="vote-container">
          <button class="vote-action-btn like-btn" :class="{ active: book.myVote === 'LIKE' }" @click="voteBook('LIKE')">
            <span class="icon">👍</span> 
            <span class="label">개추</span>
            <span class="count">{{ book.likeCount || 0 }}</span>
          </button>
          
          <button class="vote-action-btn dislike-btn" :class="{ active: book.myVote === 'DISLIKE' }" @click="voteBook('DISLIKE')">
            <span class="icon">👎</span>
            <span class="label">비추</span>
            <span class="count">{{ book.dislikeCount || 0 }}</span>
          </button>
        </div>

        <div class="action-buttons">
          <router-link v-if="book.status === 'COMPLETED'" :to="'/books/' + bookId + '/viewer'" class="btn btn-primary btn-lg shine-effect">
            📖 정주행 시작하기
          </router-link>
          
          <button v-if="book.status === 'WRITING' && isWriter" @click="completeBook" class="btn btn-outline-danger">
            ✨ 완결 짓기
          </button>
        </div>
      </div>

      <div class="container" style="max-width: 800px;">
        <!-- Sentence List -->
        <div id="sentence-list" class="sentence-list">
          <div v-for="sent in sortedSentences" :key="sent.sentenceId" class="sentence-card">
            <!-- 문장 내용 -->
            <div class="sentence-content-wrapper">
              <div v-if="editingSentenceId !== sent.sentenceId" class="sentence-content">
                <p>{{ sent.content }}</p>
                <div v-if="canEditSentence(sent)" class="edit-actions">
                  <button @click="startEditSentence(sent)" class="btn btn-ghost btn-sm" title="문장 수정">✏️</button>
                  <button @click="deleteSentence(sent)" class="btn btn-ghost btn-sm" title="문장 삭제" style="color: #ff6b6b;">🗑️</button>
                </div>
              </div>
              <div v-else class="sentence-edit-form">
                <textarea v-model="editSentenceContent" class="form-control" rows="3"></textarea>
                <div class="edit-buttons">
                  <button @click="saveSentence(sent)" class="btn btn-primary btn-sm">저장</button>
                  <button @click="cancelEditSentence" class="btn btn-outline btn-sm">취소</button>
                </div>
              </div>
            </div>

            <!-- 문장 메타 정보 -->
            <div class="sentence-meta">
              <span class="sentence-info">No.{{ sent.sequenceNo }} by {{ sent.writerNicknm || '익명#' + sent.writerId }}</span>
              <div class="vote-buttons">
                <button class="vote-btn" :class="{ 'active-like': sent.myVote === 'LIKE' }"
                  @click="voteSentence(sent, 'LIKE')">
                  👍 <span>{{ sent.likeCount || 0 }}</span>
                </button>
                <button class="vote-btn" :class="{ 'active-dislike': sent.myVote === 'DISLIKE' }"
                  @click="voteSentence(sent, 'DISLIKE')">
                  👎 <span>{{ sent.dislikeCount || 0 }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Writing Area -->
        <div v-if="book.status !== 'COMPLETED'" id="writing-area" class="writing-area">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 15px;">
            <h3 style="margin: 0; font-size: 1.1rem; font-weight: 600;">다음 문장 이어쓰기</h3>
            <!-- Typing Indicator -->
            <div v-show="activeTypers.length > 0"
              style="font-size: 0.8rem; color: var(--primary-color); display: flex; align-items: center; gap: 4px;">
              <span style="display: flex; gap: 2px;">
                <span class="typing-dot" style="animation-delay: 0s;"></span>
                <span class="typing-dot" style="animation-delay: 0.2s;"></span>
                <span class="typing-dot" style="animation-delay: 0.4s;"></span>
              </span>
              <span style="font-weight: 600; margin-left: 4px;">{{ activeTypers.join(', ') }}</span>님이 입력
              중...
            </div>
          </div>

          <!-- Guest Only -->
          <div v-if="!authStore.isAuthenticated" class="guest-only"
            style="text-align: center; padding: 25px 15px; background: rgba(0,0,0,0.02); border-radius: 12px; border: 1px dashed rgba(0,0,0,0.1);">
            <p style="color: var(--text-muted); margin-bottom: 15px; font-size: 0.9rem;">
              이야기에 참여하려면 로그인이 필요합니다.
            </p>
            <button @click="authStore.openLogin" class="btn btn-primary"
              style="padding: 8px 20px; font-size: 0.85rem; border-radius: 20px;">
              로그인하고 이어쓰기
            </button>
          </div>

          <!-- User Only -->
          <div v-else class="user-only" style="display: flex; flex-direction: column; gap: 10px;">
            <textarea v-model="newSentence" @input="handleInput" @blur="handleBlur" class="form-control" rows="3"
              :placeholder="inputPlaceholder"
              style="border-radius: 12px; font-size: 1rem; resize: none; min-height: 120px; padding: 15px;"
              :disabled="isInputDisabled"></textarea>
            <div style="display: flex; justify-content: flex-end;">
              <button class="btn btn-primary" @click="submitSentence" :disabled="isInputDisabled"
                style="padding: 12px 30px; font-size: 1rem; border-radius: 25px; font-weight: 600; box-shadow: 0 4px 6px rgba(var(--primary-rgb), 0.2);">문장
                등록</button>
            </div>
          </div>
        </div>
        <div v-else class="text-center" style="margin-top: 20px;">
          <span class="badge badge-completed">완결됨</span>
        </div>

        <!-- Comments Area -->
        <div class="card fade-in"
          style="margin-top: 40px; padding: 20px; background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%); border: 2px solid #BAE6FD;">
          <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
            <h3 style="margin: 0; font-size: 1.1rem; font-weight: 600;">감상평</h3>
            <div v-show="activeCommentTypers.length > 0" style="font-size: 0.75rem; color: var(--secondary-color);">
              💬 <span style="font-weight: 600;">{{ activeCommentTypers.join(', ') }}</span>님이 작성 중...
            </div>
          </div>

          <div v-if="!authStore.isAuthenticated" class="guest-only"
            style="text-align: center; padding: 20px; background: rgba(0,0,0,0.02); border-radius: 12px; margin-bottom: 20px;">
            <button @click="authStore.openLogin" class="btn btn-outline"
              style="padding: 6px 20px; font-size: 0.8rem; border-radius: 20px;">
              로그인하고 감상평 남기기
            </button>
          </div>

          <div v-else class="user-only">
            <div style="display: flex; flex-direction: column; gap: 10px; width: 100%;">
              <textarea v-model="newComment" @input="handleCommentInput" @blur="handleCommentBlur" class="form-control"
                rows="3" placeholder="이 소설에 대한 감상평을 남겨주세요..."
                style="border-radius: 15px; font-size: 0.95rem; padding: 15px; resize: none; min-height: 100px;"></textarea>
              <div style="display: flex; justify-content: flex-end;">
                <button class="btn btn-primary" @click="submitComment"
                  style="padding: 10px 30px; font-size: 0.9rem; border-radius: 25px; box-shadow: 0 4px 6px rgba(var(--primary-rgb), 0.2);">등록</button>
              </div>
            </div>
          </div>

          <div id="comment-list">
            <comment-node v-for="comment in comments" :key="comment.commentId" :comment="comment"
              :current-user-id="authStore.user?.userId" :user-role="authStore.user?.userRole" @reply="submitReply"
              @edit="editComment" @delete="deleteComment"></comment-node>
          </div>
        </div>
      </div>
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
    brokerURL: 'ws://localhost:8082/ws', // Direct to story-service if possible, or via Proxy if ws supported
    // Since we are proxying /ws in vite.config.js to localhost:8082, we should use window.location logic or relative path
    // Vite Proxy handles ws://localhost:3000/ws -> ws://localhost:8082/ws
    webSocketFactory: () => new SockJS('/ws'), 
    debug: function (str) {
      console.log('[STOMP]', str)
    },
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
  }

  stompClient.activate()
}

// Handlers
const handleTypingStatus = (data) => {
  // data: { nickname, isTyping }
  if (data.isTyping) {
     if (!activeTypers.value.includes(data.nickname)) activeTypers.value.push(data.nickname)
  } else {
     activeTypers.value = activeTypers.value.filter(n => n !== data.nickname)
  }
}

const handleCommentTypingStatus = (data) => {
  if (data.isTyping) {
     if (!activeCommentTypers.value.includes(data.nickname)) activeCommentTypers.value.push(data.nickname)
  } else {
     activeCommentTypers.value = activeCommentTypers.value.filter(n => n !== data.nickname)
  }
}

const handleNewSentence = (event) => {
  sentences.value.push({
    sentenceId: Date.now(), content: event.content, sequenceNo: event.sequenceNo,
    writerNicknm: event.writerNickname, writerId: event.writerId, likeCount: 0, dislikeCount: 0
  })
  if (book.value) book.value.lastWriterUserId = event.writerId
  nextTick(() => { window.scrollTo(0, document.body.scrollHeight) })
}

const handleNewComment = (comment) => {
  comments.value.unshift(comment)
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
    if (typingTimeout) clearTimeout(typingTimeout)
    sendTyping(true)
    typingTimeout = setTimeout(() => sendTyping(false), 2000)
}
const handleBlur = () => { sendTyping(false) }

const sendTyping = (status) => {
    if (!stompClient || !stompClient.connected) return
    stompClient.publish({
        destination: `/app/typing/${bookId}`,
        body: JSON.stringify({ nickname: authStore.user?.userNicknm, isTyping: status })
    })
}

const handleCommentInput = () => {
    if (commentTypingTimeout) clearTimeout(commentTypingTimeout)
    sendCommentTyping(true)
    commentTypingTimeout = setTimeout(() => sendCommentTyping(false), 2000)
}
const handleCommentBlur = () => { sendCommentTyping(false) }

const sendCommentTyping = (status) => {
    if (!stompClient || !stompClient.connected) return
    stompClient.publish({
        destination: `/app/comment-typing/${bookId}`,
        body: JSON.stringify({ nickname: authStore.user?.userNicknm, isTyping: status })
    })
}

// Actions
const submitSentence = async () => {
    if (!newSentence.value) return
    const url = links.value['append-sentence'] ? links.value['append-sentence'].href : `/books/${bookId}/sentences`
    try {
        await axios.post(url, { content: newSentence.value })
        toast.success('문장이 등록되었습니다!')
        newSentence.value = ''
        fetchBookDetail()
    } catch(e) { console.error(e) }
}

const submitComment = async () => {
    if (!newComment.value) return
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
        console.error('Vote error:', e)
        toast.error('투표 처리 중 오류가 발생했습니다.')
    }
}

const voteSentence = async (sent, voteType) => {
    if (!authStore.isAuthenticated) { authStore.openLogin(); return }
    try {
        const url = `/reactions/votes/sentences/${sent.sentenceId}`
        await axios.post(url, { voteType })
        await fetchBookDetail()
    } catch(e) {
        console.error('Sentence vote error:', e)
        toast.error('투표 처리 중 오류가 발생했습니다.')
    }
}

const completeBook = async () => {
    if (!confirm('완결하시겠습니까?')) return
    try {
        await axios.post(`/books/${bookId}/complete`)
        toast.success('완결되었습니다!')
        fetchBookDetail()
    } catch(e) { toast.error('실패') }
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
.typing-dot {
    display: inline-block;
    width: 4px;
    height: 4px;
    background-color: var(--primary-color);
    border-radius: 50%;
    margin-right: 2px;
    animation: typing-blink 1s infinite;
    vertical-align: middle;
}
@keyframes typing-blink {
    0%, 100% { opacity: 0.3; transform: scale(0.8); }
    50% { opacity: 1; transform: scale(1.2); }
}

.vote-container {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin: 30px 0;
}

.vote-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 20px;
  border: 2px solid #eee;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.vote-action-btn:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0,0,0,0.1);
}

.vote-action-btn .icon {
  font-size: 1.8rem;
  margin-bottom: 5px;
}

.vote-action-btn .label {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 600;
}

.vote-action-btn .count {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-color);
}

.vote-action-btn.active {
  border-color: var(--primary-color);
  background: #FFF0F5;
}

.vote-action-btn.active .count {
  color: var(--primary-color);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.btn-lg {
  padding: 12px 30px;
  font-size: 1.1rem;
  font-weight: 700;
  border-radius: 30px;
}

.btn-outline-danger {
  padding: 10px 20px;
  border: 2px solid #ff6b6b;
  color: #ff6b6b;
  background: transparent;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-outline-danger:hover {
  background: #ff6b6b;
  color: white;
}

.shine-effect {
  position: relative;
  overflow: hidden;
}

.shine-effect::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(to bottom right, rgba(255,255,255,0) 0%, rgba(255,255,255,0.1) 100%);
  transform: rotate(45deg);
  animation: shine 3s infinite;
}

@keyframes shine {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

/* Sentence Vote Buttons */
.vote-buttons {
  display: flex;
  gap: 8px;
}

.vote-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 2px solid #eee;
  border-radius: 20px;
  background: white;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
  transition: all 0.2s ease;
}

.vote-btn:hover {
  border-color: var(--border-color);
  background: #FFF5F8;
  transform: translateY(-2px);
}

.vote-btn.active-like {
  border-color: var(--secondary-color);
  background: linear-gradient(135deg, rgba(132, 94, 247, 0.1), rgba(151, 117, 250, 0.15));
  color: var(--secondary-color);
}

.vote-btn.active-like:hover {
  background: linear-gradient(135deg, rgba(132, 94, 247, 0.2), rgba(151, 117, 250, 0.25));
}

.vote-btn.active-dislike {
  border-color: var(--primary-color);
  background: linear-gradient(135deg, rgba(232, 93, 117, 0.1), rgba(255, 107, 157, 0.15));
  color: var(--primary-color);
}

.vote-btn.active-dislike:hover {
  background: linear-gradient(135deg, rgba(232, 93, 117, 0.2), rgba(255, 107, 157, 0.25));
}

.vote-btn span {
  font-weight: 700;
}

/* 전체 레이아웃 개선 */
.sentence-list {
  margin-bottom: 40px;
}

.sentence-card {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 25px;
  margin-bottom: 15px;
  border-left: 4px solid var(--primary-color);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transition: all 0.2s ease;
}

.sentence-card:hover {
  box-shadow: 0 8px 20px rgba(232, 93, 117, 0.12);
}

.sentence-content-wrapper {
  margin-bottom: 15px;
}

.sentence-content {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.sentence-content p {
  flex: 1;
  font-size: 1.1rem;
  line-height: 1.8;
  color: var(--text-color);
  margin: 0;
}

.edit-actions {
  display: flex;
  gap: 5px;
  opacity: 0.5;
  transition: opacity 0.2s;
}

.sentence-card:hover .edit-actions {
  opacity: 1;
}

.sentence-edit-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.edit-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sentence-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding-top: 15px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.sentence-info {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-muted);
}

.writing-area {
  background: var(--card-bg);
  border-radius: 25px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(232, 93, 117, 0.1);
  margin-bottom: 30px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .vote-container {
    gap: 15px;
  }
  
  .vote-action-btn {
    width: 70px;
    height: 70px;
    border-radius: 16px;
  }
  
  .vote-action-btn .icon {
    font-size: 1.5rem;
  }
  
  .vote-action-btn .label {
    font-size: 0.7rem;
  }
  
  .vote-action-btn .count {
    font-size: 0.8rem;
  }
  
  .sentence-card {
    padding: 18px;
    border-radius: 15px;
  }
  
  .sentence-content p {
    font-size: 1rem;
  }
  
  .sentence-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .vote-buttons {
    width: 100%;
    justify-content: flex-end;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }
  
  .btn-lg {
    padding: 10px 25px;
    font-size: 1rem;
  }
  
  .writing-area {
    padding: 20px;
    border-radius: 20px;
  }
}

@media (max-width: 480px) {
  .vote-container {
    gap: 10px;
  }
  
  .vote-action-btn {
    width: 60px;
    height: 60px;
    border-radius: 14px;
  }
  
  .vote-action-btn .icon {
    font-size: 1.3rem;
    margin-bottom: 2px;
  }
  
  .vote-action-btn .label {
    font-size: 0.65rem;
  }
  
  .vote-action-btn .count {
    font-size: 0.75rem;
  }
  
  .sentence-card {
    padding: 15px;
  }
  
  .vote-btn {
    padding: 5px 10px;
    font-size: 0.8rem;
  }
}
</style>
