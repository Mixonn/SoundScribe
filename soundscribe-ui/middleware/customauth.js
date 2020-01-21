export default function ({ store, redirect }) {
  if (!store.getters.isLoggedIn) {
    return redirect('/login')
  }
  return Promise.resolve()
}
