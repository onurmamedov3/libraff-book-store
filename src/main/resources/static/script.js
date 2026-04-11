// Element Selection
const resetPasswordBtn = document.getElementById('resetPasswordBtn');
const signInBtn = document.getElementById('signIn');
const container = document.getElementById('container');
const loginForm = document.getElementById('loginForm');
const resetForm = document.getElementById('resetForm');

// 1. Sliding Animation Logic
resetPasswordBtn.addEventListener('click', () => {
	container.classList.add("right-panel-active");
});

signInBtn.addEventListener('click', () => {
	container.classList.remove("right-panel-active");
});

// 2. JWT Login Logic (Spring Boot Connection)
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Prevent standard HTML form submission

    const fin = document.getElementById('loginFin').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const response = await fetch('http://localhost:8080/apis/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fin: fin, password: password })
        });

        if (response.ok) {
            const data = await response.json();
            
            // Save the tokens to the browser's localStorage
            localStorage.setItem('jwt', data.jwt);
            localStorage.setItem('refreshToken', data.refreshToken);
            
            alert('Login successful! Welcome to Libraff.');
            
            window.location.href = '/swagger-ui/index.html';
        } else {
            alert('Login failed: Incorrect FIN or Password.');
        }
    } catch (error) {
        console.error('Network Error:', error);
        alert('Server error. Please ensure your Spring Boot backend is running.');
    }
});

// 3. Reset Password Logic (Mocked for now)
resetForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const fin = document.getElementById('resetFin').value;
    const email = document.getElementById('resetEmail').value;
    
    // In the future, you can add a fetch() request here to hit a /reset-password API
    alert(`A password reset request has been sent to IT Support for FIN: ${fin} (${email}).`);
    
    // Slide back to the login screen automatically
    container.classList.remove("right-panel-active");
    resetForm.reset();
});