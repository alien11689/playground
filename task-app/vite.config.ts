import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

const commonConfig = {
    plugins: [react()],
};

export default defineConfig(({command}) => {
    if (command === 'serve') {
        return {
            ...commonConfig,
            server: {
                proxy: {
                    '/api': {
                        target: 'http://localhost:8080',
                        changeOrigin: true,
                        secure: false,
                        rewrite: path => path.replace(/^\/api/, ''),
                    }
                }
            }
        }
    } else {
        return {
            ...commonConfig,
        }
    }
});