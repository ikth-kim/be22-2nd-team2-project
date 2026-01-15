<template>
  <header>
    <div class="container">
      <nav>
        <router-link to="/" class="logo">
          <img src="/images/logo.png" alt="Next Page Icon" style="height:40px;"> <!-- Make sure images are copied -->
          <span>Next Page</span>
        </router-link>
        <ul class="nav-links">
          <template v-if="!authStore.isAuthenticated">
            <li style="display: flex; gap: 10px;">
              <button @click="authStore.openLogin" class="btn btn-outline"
                style="padding: 5px 15px; font-size: 0.9rem;">로그인</button>
              <button @click="authStore.openSignup" class="btn btn-submit btn-primary"
                style="padding: 5px 15px; font-size: 0.9rem;">회원가입</button>
            </li>
          </template>
          <template v-else>
            <li style="display: flex; gap: 15px;">
              <router-link to="/books/new">소설 쓰기</router-link>
              <router-link to="/mypage">마이페이지</router-link>
              <a href="#" @click.prevent="logout">로그아웃</a>
            </li>
          </template>
        </ul>
      </nav>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const logout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
/* Inherits from global main.css */
header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(255, 211, 224, 0.5);
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}
.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-color);
}

.logo span {
  font-family: 'Gaegu', cursive;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--primary-color);
}

.router-link-active {
    font-weight: 700;
    color: var(--primary-color) !important;
}

.nav-links a {
  text-decoration: none;
  color: var(--text-color);
  font-weight: 500;
  transition: color 0.3s;
}

.nav-links a:hover {
  color: var(--primary-color);
}
</style>
