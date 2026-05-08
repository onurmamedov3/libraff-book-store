// ─────────────────────────────────────────────────────────────────
//  Element references
// ─────────────────────────────────────────────────────────────────
const resetPasswordBtn  = document.getElementById('resetPasswordBtn');
const signInBtn         = document.getElementById('signIn');
const container         = document.getElementById('container');
const loginForm         = document.getElementById('loginForm');
const resetForm         = document.getElementById('resetForm');

// OTP modal
const otpModal          = document.getElementById('otpModal');
const otpEmailHint      = document.getElementById('otpEmailHint');
const otpBoxes          = document.querySelectorAll('.otp-box');
const otpError          = document.getElementById('otpError');
const verifyOtpBtn      = document.getElementById('verifyOtpBtn');
const resendBtn         = document.getElementById('resendBtn');
const cancelOtpBtn      = document.getElementById('cancelOtpBtn');

// New-password modal
const newPasswordModal  = document.getElementById('newPasswordModal');
const newPasswordInput  = document.getElementById('newPassword');
const confirmPwInput    = document.getElementById('confirmPassword');
const pwError           = document.getElementById('pwError');
const savePasswordBtn   = document.getElementById('savePasswordBtn');

// State shared across steps
let pendingFin   = '';
let pendingEmail = '';
let resetToken   = '';

// ─────────────────────────────────────────────────────────────────
//  1. Sliding-panel animation
// ─────────────────────────────────────────────────────────────────
resetPasswordBtn.addEventListener('click', () => container.classList.add('right-panel-active'));
signInBtn.addEventListener('click', () => container.classList.remove('right-panel-active'));

// ─────────────────────────────────────────────────────────────────
//  2. Login form
// ─────────────────────────────────────────────────────────────────
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fin      = document.getElementById('loginFin').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    try {
        const response = await fetch('http://localhost:8080/apis/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fin, password }),
        });

        if (response.ok) {
            const data = await response.json();
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

// ─────────────────────────────────────────────────────────────────
//  3. Reset flow — Step 1: request OTP
// ─────────────────────────────────────────────────────────────────
resetForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const submitBtn = document.getElementById('resetSubmitBtn');
    const fin       = document.getElementById('resetFin').value.trim();
    const email     = document.getElementById('resetEmail').value.trim();

    submitBtn.disabled = true;
    submitBtn.textContent = 'Sending…';

    try {
        const response = await fetch('http://localhost:8080/apis/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fin, email }), // ✅ Lowercase matches DTO
        });

        if (response.ok) {
            pendingFin   = fin;
            pendingEmail = email;

            const [user, domain] = email.split('@');
            const masked = user.slice(0, 2) + '**@' + domain;
            otpEmailHint.textContent = masked;

            clearOtpBoxes();
            openModal(otpModal);
            otpBoxes[0].focus();
        } else {
            const body = await response.json().catch(() => ({}));
            alert(body.message || 'Could not send reset code. Please check your FIN and email.');
        }
    } catch (error) {
        console.error('Network Error:', error);
        alert('Server error. Please ensure your Spring Boot backend is running.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Send Reset Code';
    }
});

// ─────────────────────────────────────────────────────────────────
//  4. OTP box keyboard UX (✅ Fixed broken JS syntax)
// ─────────────────────────────────────────────────────────────────
otpBoxes.forEach((box, index) => {
    box.addEventListener('keypress', (e) => {
        if (!/[0-9]/.test(e.key)) e.preventDefault();
    });

    box.addEventListener('input', (e) => {
        const val = e.target.value.replace(/\D/g, '');
        box.value = val ? val[val.length - 1] : '';
        box.classList.toggle('filled', box.value !== '');
        clearOtpError();

        if (box.value && index < otpBoxes.length - 1) {
            otpBoxes[index + 1].focus();
        }
    });

    box.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !box.value && index > 0) {
            otpBoxes[index - 1].value = '';
            otpBoxes[index - 1].classList.remove('filled');
            otpBoxes[index - 1].focus();
        }
        if (e.key === 'ArrowLeft' && index > 0) otpBoxes[index - 1].focus();
        if (e.key === 'ArrowRight' && index < otpBoxes.length - 1) otpBoxes[index + 1].focus();
    });

    box.addEventListener('paste', (e) => {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
        pasted.split('').slice(0, otpBoxes.length - index).forEach((char, i) => {
            otpBoxes[index + i].value = char;
            otpBoxes[index + i].classList.add('filled');
        });
        const nextEmpty = [...otpBoxes].findIndex(b => !b.value);
        if (nextEmpty !== -1) otpBoxes[nextEmpty].focus();
        else otpBoxes[otpBoxes.length - 1].focus();
    });
});

// ─────────────────────────────────────────────────────────────────
//  5. Reset flow — Step 2: verify OTP
// ─────────────────────────────────────────────────────────────────
verifyOtpBtn.addEventListener('click', async () => {
    const code = [...otpBoxes].map(b => b.value).join('');
    if (code.length < 6) {
        showOtpError('Please fill in all 6 digits.');
        return;
    }

    verifyOtpBtn.disabled = true;
    verifyOtpBtn.textContent = 'Verifying…';

    try {
        const response = await fetch('http://localhost:8080/apis/auth/verify-code', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fin: pendingFin, otpCode: code }), // ✅ Lowercase fin
        });

        if (response.ok) {
            const data = await response.json();
            resetToken = data.resetToken;
            closeModal(otpModal);
            openModal(newPasswordModal);
            newPasswordInput.focus();
        } else if (response.status === 400) {
            showOtpError('Invalid or expired code. Try again.');
            shakeOtpBoxes();
        } else {
            showOtpError('Something went wrong. Please try again.');
        }
    } catch (error) {
        console.error('Network Error:', error);
        showOtpError('Server error. Please try again.');
    } finally {
        verifyOtpBtn.disabled = false;
        verifyOtpBtn.textContent = 'Verify Code';
    }
});

// ─────────────────────────────────────────────────────────────────
//  6. Resend code
// ─────────────────────────────────────────────────────────────────
resendBtn.addEventListener('click', async (e) => {
    e.preventDefault();
    resendBtn.textContent = 'Sending…';
    resendBtn.style.pointerEvents = 'none';

    try {
        const response = await fetch('http://localhost:8080/apis/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fin: pendingFin, email: pendingEmail }),
        });

        if (response.ok) {
            clearOtpBoxes();
            clearOtpError();
            otpBoxes[0].focus();
            otpError.style.color = '#2e7d32';
            otpError.textContent = 'New code sent!';
            setTimeout(() => {
                otpError.style.color = '';
                otpError.textContent = '';
            }, 3000);
        } else {
            showOtpError('Could not resend. Please wait and try again.');
        }
    } catch {
        showOtpError('Server error. Please try again.');
    } finally {
        setTimeout(() => {
            resendBtn.textContent = 'Resend';
            resendBtn.style.pointerEvents = '';
        }, 30000);
    }
});

// ─────────────────────────────────────────────────────────────────
//  7. Cancel OTP → back to login
// ─────────────────────────────────────────────────────────────────
cancelOtpBtn.addEventListener('click', () => {
    closeModal(otpModal);
    container.classList.remove('right-panel-active');
    resetForm.reset();
    clearOtpBoxes();
    clearOtpError();
});

// ─────────────────────────────────────────────────────────────────
//  8. Reset flow — Step 3: save new password
// ─────────────────────────────────────────────────────────────────
savePasswordBtn.addEventListener('click', async () => {
    const newPw  = newPasswordInput.value;
    const confPw = confirmPwInput.value;
    pwError.textContent = '';

    if (newPw.length < 8) {
        pwError.textContent = 'Password must be at least 8 characters.';
        return;
    }
    if (newPw !== confPw) {
        pwError.textContent = 'Passwords do not match.';
        return;
    }

    savePasswordBtn.disabled = true;
    savePasswordBtn.textContent = 'Saving…';

    try {
        // ✅ Changed to POST & moved resetToken into JSON body to match backend DTO
        const response = await fetch('http://localhost:8080/apis/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resetToken, newPassword: newPw }),
        });

        if (response.ok) {
            closeModal(newPasswordModal);
            alert('Password updated successfully! Please log in with your new password.');
            container.classList.remove('right-panel-active');
            resetForm.reset();
            newPasswordInput.value = '';
            confirmPwInput.value = '';
            resetToken = '';
        } else {
            const body = await response.json().catch(() => ({}));
            pwError.textContent = body.message || 'Failed to update password. Your reset link may have expired.';
        }
    } catch (error) {
        console.error('Network Error:', error);
        pwError.textContent = 'Server error. Please try again.';
    } finally {
        savePasswordBtn.disabled = false;
        savePasswordBtn.textContent = 'Save Password';
    }
});

// ─────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────
function openModal(modal) { modal.classList.add('active'); }
function closeModal(modal) { modal.classList.remove('active'); }
function clearOtpBoxes() {
    otpBoxes.forEach(b => { b.value = ''; b.classList.remove('filled', 'error'); });
}
function showOtpError(msg) { otpError.textContent = msg; }
function clearOtpError() {
    otpError.textContent = '';
    otpBoxes.forEach(b => b.classList.remove('error'));
}
function shakeOtpBoxes() {
    otpBoxes.forEach(b => {
        b.classList.add('error');
        setTimeout(() => b.classList.remove('error'), 500);
    });
}