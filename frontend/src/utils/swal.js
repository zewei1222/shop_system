// src/utils/swal.js
import Swal from 'sweetalert2'

// 基礎配置：自動適應深色模式
const getOptions = () => {
    const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
    return {
        background: isDark ? '#1e293b' : '#ffffff',
        color: isDark ? '#f8fafc' : '#111827',
        confirmButtonColor: '#4f46e5',
        cancelButtonColor: '#ef4444',
    }
}

export const showSuccess = (title) => {
    return Swal.fire({
        ...getOptions(),
        icon: 'success',
        title: title,
        showConfirmButton: false,
        timer: 1500
    })
}

export const showError = (title) => {
    return Swal.fire({
        ...getOptions(),
        icon: 'error',
        title: '發生錯誤',
        text: title
    })
}

export const showConfirm = async (text) => {
    const result = await Swal.fire({
        ...getOptions(),
        title: '確定嗎？',
        text: text,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        reverseButtons: true
    })
    return result.isConfirmed
}