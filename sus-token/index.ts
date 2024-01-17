export const required = <T>(val?: T | null, errorFactory?: () => Error): T => {
  if (val === undefined || val === null) {
    throw errorFactory ? errorFactory() : new Error('Null or undefined value');
  }

  return val;
};
