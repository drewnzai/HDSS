import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLoginMutation } from '../../redux/AuthApi';
import "./Login.css"

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const [login, { isLoading, error }] = useLoginMutation();
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      await login({ username, password }).unwrap();
      navigate('/', { replace: true });
    } catch {
      // The error state below reflects the failed request.
    }
  };

  return (
    <div className="login-page">
      <header className="login-page__header">
        <div className="login-page__brand">
          <span className="login-page__brand-name">HDSS</span>
          <span className="login-page__brand-divider">·</span>
          <span className="login-page__brand-context">Field Portal</span>
        </div>
      </header>

      <main className="login-page__main">
        <section className="login-card" aria-labelledby="login-title">
          <div className="login-card__header">

            <h1 id="login-title" className="login-card__title">
              Sign in
            </h1>

            <p className="login-card__description">
              Enter your field credentials to access household records.
            </p>
          </div>

          <form
            className="login-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="login-form__fields">
              <div className="form-field">
                <label className="form-field__label" htmlFor="username">
                  Username
                </label>

                <input
                  id="username"
                  name="username"
                  type="text"
                  autoComplete="username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  className="form-field__input"
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label className="form-field__label" htmlFor="password">
                  Password
                </label>

                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  className="form-field__input"
                  required
                  disabled={isLoading}
                />
              </div>
            </div>

            {error && (
              <div
                className="login-form__error"
                role="alert"
                aria-live="polite"
              >
                Incorrect username or password.
              </div>
            )}

            <button
              type="submit"
              className="login-form__submit"
              disabled={isLoading}
            >
              {isLoading ? 'Signing in…' : 'Sign in'}
            </button>
          </form>
        </section>
      </main>

      <footer className="login-page__footer">
        <span>Health and Demographic Surveillance System</span>
      </footer>
    </div>
  );
}

export default Login;