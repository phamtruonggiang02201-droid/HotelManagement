/**
 * Staff Task Management
 * Manages picking up tasks from the pool and updating their progress
 */
const staffTaskService = {
    async getMyTasks() {
        try {
            const response = await fetch('/api/staff/tasks/my');
            return await response.json();
        } catch (error) {
            console.error('Error fetching tasks:', error);
            return [];
        }
    },

    async getPoolTasks() {
        try {
            const response = await fetch('/api/staff/tasks/pool');
            return await response.json();
        } catch (error) {
            console.error('Error fetching pool:', error);
            return [];
        }
    },

    async claimTask(taskId) {
        try {
            const response = await fetch(`/api/staff/tasks/${taskId}/claim`, { method: 'POST' });
            return await response.json();
        } catch (error) {
            console.error('Error claiming task:', error);
            return { success: false };
        }
    },

    async updateTaskStatus(taskId, status) {
        try {
            const response = await fetch(`/api/staff/tasks/${taskId}/status?status=${status}`, { method: 'PUT' });
            return await response.json();
        } catch (error) {
            console.error('Error updating status:', error);
            return { success: false };
        }
    }
};
