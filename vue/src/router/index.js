import Vue from 'vue'
import nprogress from 'nprogress'
import VueRouter from 'vue-router'
import 'nprogress/nprogress.css'
import { detectDevice } from "../utils/index";

Vue.use(VueRouter)

// 解决导航栏或者底部导航tabBar中的vue-router在3.0版本以上频繁点击菜单报错的问题。
const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}

const mRoutes = [
    {
      path: '/mobile', name: 'Layout', component: () => import('../views/mobile/layout'),
      children:[
          { path: 'home', name: 'home', meta: { name: '系统首页' }, component: () => import('../views/mobile/home') },
          { path: 'activity', name: 'activity', meta: { name: '活动信息' }, component: () => import('../views/mobile/activity') },
          { path: 'person', name: 'person', meta: { name: '个人信息' }, component: () => import('../views/mobile/person') },
          { path: 'articleDetail', name: 'articleDetail', meta: { name: '博客详情页' }, component: () => import('../views/mobile/articleDetail') },
          { path: 'publish', name: 'publish', meta: { name: '发布博客' }, component: () => import('../views/mobile/publish') },
        { path: 'myBlog', name: 'myBlog', meta: { name: '我发表的博客' }, component: () => import('../views/mobile/myBlog') },
          { path: 'activityDetail', name: 'activityDetail', meta: { name: '活动详情' }, component: () => import('../views/mobile/activityDetail') },
          { path: 'myActivity', name: 'myActivity', meta: { name: '我报名的活动' }, component: () => import('../views/mobile/myActivity') },
        { path: 'myLike', name: 'myLike', meta: { name: '我的点赞' }, component: () => import('../views/mobile/myLike') },
        { path: 'myCollect', name: 'myCollect', meta: { name: '我的收藏' }, component: () => import('../views/mobile/myCollection') },
        { path: 'myComment', name: 'myComment', meta: { name: '我的评论' }, component: () => import('../views/mobile/myComment') }
      ]
    },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
]

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Manager.vue'),
    children: [
      { path: '403', name: 'NoAuth', meta: { name: '无权限' }, component: () => import('../views/manager/403') },
      { path: 'home', name: 'Home', meta: { name: '系统首页' }, component: () => import('../views/manager/Home') },
      { path: 'admin', name: 'Admin', meta: { name: '管理员信息' }, component: () => import('../views/manager/Admin') },
      { path: 'adminPerson', name: 'AdminPerson', meta: { name: '个人信息' }, component: () => import('../views/manager/AdminPerson') },
      { path: 'password', name: 'Password', meta: { name: '修改密码' }, component: () => import('../views/manager/Password') },
      { path: 'notice', name: 'Notice', meta: { name: '公告信息' }, component: () => import('../views/manager/Notice') },
      { path: 'user', name: 'User', meta: { name: '用户信息' }, component: () => import('../views/manager/User') },
      { path: 'category', name: 'Category', meta: { name: '用户信息' }, component: () => import('../views/manager/Category') },
      { path: 'blog', name: 'Blog', meta: { name: '博客信息' }, component: () => import('../views/manager/Blog') },
      { path: 'activity', name: 'Activity', meta: { name: '活动信息' }, component: () => import('../views/manager/Activity') },
      { path: 'comment', name: 'Comment', meta: { name: '评论信息' }, component: () => import('../views/manager/Comment') },
      { path: 'activitySign', name: 'ActivitySign', meta: { name: '评论信息' }, component: () => import('../views/manager/ActivitySign') },
    ],

  },
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/Front.vue'),
    children: [
      { path: 'home', name: 'Home', meta: { name: '系统首页' }, component: () => import('../views/front/Home') },
      { path: 'person', name: 'Person', meta: { name: '个人信息' }, component: () => import('../views/front/Person') },
      { path: 'blogDetail', name: 'BlogDetail', meta: { name: '活动信息' }, component: () => import('../views/front/BlogDetail') },
      { path: 'activity', name: 'Activity', meta: { name: '活动中心' }, component: () => import('../views/front/Activity') },
      { path: 'search', name: 'Search', meta: { name: '活动中心' }, component: () => import('../views/front/Search.vue') },
      { path: 'activityDetail', name: 'ActivityDetail', meta: { name: '活动中心' }, component: () => import('../views/front/ActivityDetail') },
      { path: 'newBlog', name: 'NewBlog', meta: { name: '活动中心' }, component: () => import('../views/front/NewBlog.vue') },
    ]
  },
  { path: '/login', name: 'Login', meta: { name: '登录' }, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: '注册' }, component: () => import('../views/Register.vue') },
  { path: '*', name: 'NotFound', meta: { name: '无法访问' }, component: () => import('../views/404.vue') },
]

const  getRoutes = () => {
  const type = detectDevice()
  if (type === 'Mobile') {
    return mRoutes
  } else {
    return routes
  }
}

const router = new VueRouter({
  mode: 'hash',
  routes: getRoutes(),
  scrollBehavior(to, from, savedPosition) {
    // 如果 savedPosition 存在，则返回 savedPosition，通常是浏览器的前进/后退按钮
    if (savedPosition) {
      return savedPosition
    } else {
      // 对于所有路由导航，返回 { x: 0, y: 0 } 使得页面滚动到顶部
      return {x: 0, y: 0}
    }
  }
})
// vue在刷新后如何切换移动端和PC端
/*注：不需要前台的项目，可以注释掉该路由守卫
路由守卫*/
router.beforeEach((to ,from, next) => {
  // console.log(to,from)
  // if(type === 'Mobile') {
  //   debugger
  //   router.push('/mobile/home')
  // } else {
  //   router.push('/front/home')
  // }
  nprogress.start()
  let user = JSON.parse(localStorage.getItem("xm-user") || '{}');
  if (to.path === '/') {
    if (user.role) {
      nprogress.done()
      if (user.role === 'USER') {
        const type = detectDevice()
        if(type === 'Mobile') {
          next('/mobile/home')
        } else {
          next('/front/home')
        }
      } else {
        next('/home')
      }
    } else {
      nprogress.done()
      next('/login')
    }
  } else {
      nprogress.done()
      next()
    }
})
/** *
 * 后置守卫
 * **/
router.afterEach(() => {
  nprogress.done()
})
export default router
