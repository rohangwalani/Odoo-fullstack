import { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { OTPInput } from '../components/OTPInput';
import { Button } from '../components/Button';
import axiosInstance from '../api/axiosInstance';
import toast from 'react-hot-toast';

export const EmailVerificationPage = () => {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [countdown, setCountdown] = useState(30);

  const location = useLocation();
  const navigate = useNavigate();
  const email = location.state?.email || 'your email';

  useEffect(() => {
    let timer;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  const handleVerify = async (e) => {
    e.preventDefault();
    const otpString = otp.join('');
    
    if (otpString.length !== 6) {
      setError('Please enter a 6-digit OTP');
      return;
    }
    setError('');
    setLoading(true);

    try {
      // Mock API call
      // await axiosInstance.post('/auth/verify-email', { email, otp: otpString });
      await new Promise((resolve) => setTimeout(resolve, 1500));
      
      toast.success('Email verified successfully!', { className: 'custom-toast' });
      navigate('/reset-password');
    } catch (err) {
      // handled
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (countdown > 0) return;
    
    try {
      // Mock API call
      // await axiosInstance.post('/auth/resend-otp', { email });
      setCountdown(30);
      toast.success('OTP resent successfully!', { className: 'custom-toast' });
    } catch (error) {
      // handled
    }
  };

  return (
    <AuthLayout title="Verify Email" subtitle={`We sent a 6-digit code to ${email}`}>
      <form onSubmit={handleVerify} className="flex-col gap-6">
        <OTPInput length={6} value={otp} onChange={setOtp} error={error} />

        <Button type="submit" loading={loading}>
          Verify OTP
        </Button>

        <div className="text-center mt-2 flex-col gap-2">
          {countdown > 0 ? (
            <div className="countdown-text">
              Resend code in <span className="countdown-timer">00:{countdown.toString().padStart(2, '0')}</span>
            </div>
          ) : (
            <button
              type="button"
              onClick={handleResend}
              className="auth-link text-sm bg-transparent border-none p-0 cursor-pointer"
            >
              Resend OTP
            </button>
          )}
          
          <Link to="/login" className="auth-link text-sm mt-4">
            Back to Login
          </Link>
        </div>
      </form>
    </AuthLayout>
  );
};
