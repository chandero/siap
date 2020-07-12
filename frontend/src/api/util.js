export function generatePassword () {
  let CharacterSet = ''
  let password = ''
  const size = 8
  CharacterSet += 'abcdefghijklmnopqrstuvwxyz'
  CharacterSet += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
  CharacterSet += '0123456789'
  for (let i = 0; i < size; i++) {
    password += CharacterSet.charAt(Math.floor(Math.random() * CharacterSet.length))
  }
  return password
}
