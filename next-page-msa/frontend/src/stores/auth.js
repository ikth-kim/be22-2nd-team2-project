import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: null,
        accessToken: localStorage.getItem('accessToken') || null,
        showLoginModal: false,
        showSignupModal: false
    }),
    getters: {
        isAuthenticated: (state) => !!state.accessToken,
    },
    actions: {
        async login(email, password, autoLogin) {
            try {
                const response = await axios.post('/auth/login', {
                    userEmail: email,
                    userPw: password
                })

                const { accessToken, refreshToken } = response.data.data
                this.setSession(accessToken, refreshToken)

                if (autoLogin) {
                    localStorage.setItem('autoLoginEnabled', 'true')
                } else {
                    localStorage.removeItem('autoLoginEnabled')
                }

                await this.fetchUserProfile()
                return true
            } catch (error) {
                throw error
            }
        },

        async signup(email, nickname, password) {
            try {
                await axios.post('/auth/signup', {
                    userEmail: email,
                    userNicknm: nickname,
                    userPw: password
                })
                // Auto login after signup
                return await this.login(email, password)
            } catch (error) {
                throw error
            }
        },

        async logout() {
            this.user = null
            this.accessToken = null
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('autoLoginEnabled')
        },

        async checkAutoLogin() {
            const autoLoginEnabled = localStorage.getItem('autoLoginEnabled') === 'true'
            const refreshToken = localStorage.getItem('refreshToken')

            if (autoLoginEnabled && (refreshToken || !this.accessToken)) {
                try {
                    const response = await axios.post('/auth/refresh', {}, {
                        headers: { 'Cookie': `refreshToken=${refreshToken}` } // Note: Browsers might not let you set Cookie header manually in JS, standard is HttpOnly cookies sent automatically. 
                        // However, previous code manually sent it. If cookie is HttpOnly, this header setting is useless.
                        // If the backend expects it in header or cookie, we need to match.
                        // The previous code: headers: { 'Cookie': 'refreshToken=' + refreshToken } 
                        // This suggests it's NOT an HttpOnly cookie or the backend reads it from header.
                    })

                    const { accessToken: newAccess, refreshToken: newRefresh } = response.data.data
                    this.setSession(newAccess, newRefresh)
                    await this.fetchUserProfile()
                } catch (e) {
                    this.logout()
                }
            } else if (this.accessToken) {
                await this.fetchUserProfile()
            }
        },

        async fetchUserProfile() {
            if (!this.accessToken) return
            try {
                // Assuming there is an endpoint to get 'me'. If not, we might need to rely on token decoding or Gateway header
                // Member Service: /api/members/me
                const response = await axios.get('/members/me')
                this.user = response.data.data
            } catch (e) {
                // Token invalid
                this.logout()
            }
        },

        setSession(accessToken, refreshToken) {
            this.accessToken = accessToken
            this.user = null // Will be fetched
            localStorage.setItem('accessToken', accessToken)
            localStorage.setItem('refreshToken', refreshToken)
        },

        openLogin() { this.showLoginModal = true },
        closeLogin() { this.showLoginModal = false },
        openSignup() { this.showSignupModal = true },
        closeSignup() { this.showSignupModal = false }
    }
})
