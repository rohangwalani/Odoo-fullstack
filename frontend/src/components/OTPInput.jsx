import { useRef, useEffect } from 'react';

export const OTPInput = ({ length = 6, value, onChange, error }) => {
  const inputRefs = useRef([]);

  useEffect(() => {
    if (inputRefs.current[0]) {
      inputRefs.current[0].focus();
    }
  }, []);

  const handleChange = (e, index) => {
    const val = e.target.value;
    if (isNaN(val)) return;

    const newValue = [...value];
    newValue[index] = val.substring(val.length - 1);
    onChange(newValue);

    if (val && index < length - 1) {
      inputRefs.current[index + 1].focus();
    }
  };

  const handleKeyDown = (e, index) => {
    if (e.key === 'Backspace' && !value[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').replace(/\D/g, '').substring(0, length);
    if (!pastedData) return;

    const newValue = [...value];
    for (let i = 0; i < pastedData.length; i++) {
      newValue[i] = pastedData[i];
    }
    onChange(newValue);

    const nextFocusIndex = Math.min(pastedData.length, length - 1);
    inputRefs.current[nextFocusIndex].focus();
  };

  return (
    <div className="flex-col gap-2">
      <div className="otp-container" onPaste={handlePaste}>
        {value.map((digit, index) => (
          <input
            key={index}
            ref={(el) => (inputRefs.current[index] = el)}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={digit}
            onChange={(e) => handleChange(e, index)}
            onKeyDown={(e) => handleKeyDown(e, index)}
            className={`otp-box ${digit ? 'filled' : ''} ${error ? 'has-error' : ''}`}
          />
        ))}
      </div>
      {error && (
        <div className="field-error justify-center">
          <span>{error}</span>
        </div>
      )}
    </div>
  );
};
