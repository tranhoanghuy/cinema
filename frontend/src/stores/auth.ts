import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import Keycloak from 'keycloak-js'
import type { KeycloakUserInfo } from '@/types'

const KC_CONFIG = {
  url:      import.meta.env.VITE_KEYCLOAK_URL    || 'http://localhost:8180',
  realm:    import.meta.env.VITE_KEYCLOAK_REALM  || 'cinetix',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT || 'cinetix-frontend'
}

export const useAuthStore = defineStore('auth', () => {
  const keycloak = ref<Keycloak | null>(null)
  const token    = ref<string | null | undefined>(null)
  const userInfo = ref<KeycloakUserInfo | null>(null)
  const ready    = ref(false)

  const isAuthenticated = computed(() => !!token.value)
  const userId          = computed(() => keycloak.value?.subject ?? null)
  const username        = computed(() => userInfo.value?.preferred_username ?? '')
  const fullName        = computed(() => userInfo.value?.name ?? username.value)
  const isAdmin         = computed(() => {
    const roles = keycloak.value?.realmAccess?.roles ?? []
    return roles.includes('ADMIN')
  })

  async function init(): Promise<void> {
    const kc = new Keycloak(KC_CONFIG)
    keycloak.value = kc

    try {
      const authenticated = await kc.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256'
      })

      if (authenticated) {
        token.value    = kc.token
        userInfo.value = await kc.loadUserInfo() as KeycloakUserInfo
        _scheduleRefresh()
      }
    } catch {
      // Keycloak not reachable (offline dev) — continue unauthenticated
    } finally {
      ready.value = true
    }
  }

  function login(redirectUri = window.location.href): void {
    keycloak.value?.login({ redirectUri })
  }

  function logout(): void {
    token.value    = null
    userInfo.value = null
    keycloak.value?.logout({ redirectUri: window.location.origin })
  }

  async function getToken(): Promise<string | undefined> {
    if (!keycloak.value) return undefined
    try {
      await keycloak.value.updateToken(30)
      token.value = keycloak.value.token
    } catch {
      logout()
    }
    return keycloak.value.token
  }

  function _scheduleRefresh(): void {
    setInterval(async () => {
      try {
        const refreshed = await keycloak.value!.updateToken(60)
        if (refreshed) token.value = keycloak.value!.token
      } catch {
        logout()
      }
    }, 30_000)
  }

  return { ready, isAuthenticated, userId, username, fullName, isAdmin,
           token, userInfo, init, login, logout, getToken }
})
